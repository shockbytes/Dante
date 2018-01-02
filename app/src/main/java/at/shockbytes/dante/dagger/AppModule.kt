package at.shockbytes.dante.dagger

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.backup.GoogleDriveBackupManager
import at.shockbytes.dante.backup.google.GoogleSignInManager
import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.network.google.gson.BookBackupSerializer
import at.shockbytes.dante.network.google.gson.GoogleBooksSuggestionResponseDeserializer
import at.shockbytes.dante.util.AppParams
import at.shockbytes.dante.util.DanteRealmMigration
import at.shockbytes.dante.util.books.BookManager
import at.shockbytes.dante.util.books.BookSuggestion
import at.shockbytes.dante.util.books.RealmBookManager
import at.shockbytes.dante.util.tracking.KeenTracker
import at.shockbytes.dante.util.tracking.Tracker
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import javax.inject.Named
import javax.inject.Singleton

/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideBookManager(bookDownloader: BookDownloader, realm: Realm): BookManager {
        return RealmBookManager(bookDownloader, realm)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(BookSuggestion::class.java, GoogleBooksSuggestionResponseDeserializer())
                .create()
    }

    @Provides
    @Singleton
    @Named("backup_gson")
    fun provideBackupGson(): Gson {
        return try {
            GsonBuilder()
                    .setExclusionStrategies(object : ExclusionStrategy {
                        override fun shouldSkipField(f: FieldAttributes): Boolean {
                            return f.declaringClass == RealmObject::class.java
                        }

                        override fun shouldSkipClass(clazz: Class<*>) = false
                    })
                    .registerTypeAdapter(Class.forName("io.realm.BookRealmProxy"), BookBackupSerializer())
                    .create()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Gson()
        }
    }

    @Provides
    @Singleton
    fun provideTracker(): Tracker {
        return KeenTracker(app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideRealm(): Realm {
        return Realm.getInstance(RealmConfiguration.Builder()
                .schemaVersion(AppParams.realmSchemaVersion)
                .migration(DanteRealmMigration())
                .build())
    }

    @Provides
    @Singleton
    fun provideBackupManager(preferences: SharedPreferences,
                             signInManager: GoogleSignInManager,
                             @Named("backup_gson") gson: Gson): BackupManager {
        return GoogleDriveBackupManager(preferences, signInManager, gson)
    }

    @Provides
    @Singleton
    fun provideGoogleSignInManager(prefs: SharedPreferences): GoogleSignInManager {
        return GoogleSignInManager(prefs)
    }

}

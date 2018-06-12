package at.shockbytes.dante.dagger

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.backup.GoogleDriveBackupManager
import at.shockbytes.dante.books.BookSuggestion
import at.shockbytes.dante.network.google.gson.BookBackupSerializer
import at.shockbytes.dante.network.google.gson.GoogleBooksSuggestionResponseDeserializer
import at.shockbytes.dante.signin.GoogleSignInManager
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.util.DanteRealmMigration
import at.shockbytes.dante.util.DanteSettings
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
    fun provideApplication(): Application {
        return app
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideDanteSettings(): DanteSettings {
        return DanteSettings(app.applicationContext)
    }

    @Provides
    @Singleton
    @Named("gsonDownload")
    fun provideGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(BookSuggestion::class.java, GoogleBooksSuggestionResponseDeserializer())
                .create()
    }

    @Provides
    @Singleton
    @Named("gsonBackup")
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
                .schemaVersion(DanteRealmMigration.migrationVersion)
                .migration(DanteRealmMigration())
                .build())
    }

    @Provides
    @Singleton
    fun provideBackupManager(preferences: SharedPreferences,
                             signInManager: SignInManager,
                             @Named("gsonBackup") gson: Gson): BackupManager {
        // TODO Remove this ugly cast, but for now this is the only supported SignInManager
        return GoogleDriveBackupManager(preferences, signInManager as GoogleSignInManager, gson)
    }

    @Provides
    @Singleton
    fun provideGoogleSignInManager(prefs: SharedPreferences): SignInManager {
        return GoogleSignInManager(prefs, app.applicationContext)
    }

}

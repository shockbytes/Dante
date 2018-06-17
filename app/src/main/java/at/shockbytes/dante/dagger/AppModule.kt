package at.shockbytes.dante.dagger

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.backup.GoogleDriveBackupManager
import at.shockbytes.dante.book.BookSuggestion
import at.shockbytes.dante.network.google.gson.GoogleBooksSuggestionResponseDeserializer
import at.shockbytes.dante.signin.GoogleSignInManager
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.util.DanteRealmMigration
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.tracking.FirebaseTracker
import at.shockbytes.dante.util.tracking.Tracker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
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
    fun provideDanteSettings(sharedPreferences: SharedPreferences): DanteSettings {
        return DanteSettings(app.applicationContext, sharedPreferences)
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
    fun provideTracker(): Tracker {
        return FirebaseTracker(app.applicationContext)
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
                             signInManager: SignInManager): BackupManager {
        // TODO Remove this ugly cast, but for now this is the only supported SignInManager
        return GoogleDriveBackupManager(preferences, signInManager as GoogleSignInManager, Gson())
    }

    @Provides
    @Singleton
    fun provideGoogleSignInManager(prefs: SharedPreferences): SignInManager {
        return GoogleSignInManager(prefs, app.applicationContext)
    }

}

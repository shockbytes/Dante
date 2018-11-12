package at.shockbytes.dante.dagger

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.backup.GoogleDriveBackupManager
import at.shockbytes.dante.billing.GoogleInAppBillingService
import at.shockbytes.dante.billing.InAppBillingService
import at.shockbytes.dante.book.BookSuggestion
import at.shockbytes.dante.network.google.gson.GoogleBooksSuggestionResponseDeserializer
import at.shockbytes.dante.signin.GoogleSignInManager
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.ui.image.GlideImageLoader
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.util.DanteRealmMigration
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.flagging.FeatureFlagging
import at.shockbytes.dante.util.flagging.FirebaseFeatureFlagging
import at.shockbytes.dante.util.scheduler.AppSchedulerFacade
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.tracking.FirebaseTracker
import at.shockbytes.dante.util.tracking.DebugTracker
import at.shockbytes.dante.util.tracking.Tracker
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Named
import javax.inject.Singleton

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
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
        return if (BuildConfig.DEBUG) {
            DebugTracker()
        } else {
            FirebaseTracker(app.applicationContext)
        }
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
                             schedulerFacade: SchedulerFacade): BackupManager {
        return GoogleDriveBackupManager(preferences,
                signInManager as GoogleSignInManager,
                schedulerFacade,
                Gson())
    }

    @Provides
    @Singleton
    fun provideGoogleSignInManager(prefs: SharedPreferences): SignInManager {
        return GoogleSignInManager(prefs, app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideFeatureFlagging(remoteConfig: FirebaseRemoteConfig): FeatureFlagging {
        return FirebaseFeatureFlagging(remoteConfig)
    }

    @Provides
    @Singleton
    fun provideInAppBillingService(): InAppBillingService {
        return GoogleInAppBillingService()
    }

    @Provides
    @Singleton
    fun provideImageLoader(): ImageLoader {
        return GlideImageLoader
    }

    @Provides
    @Singleton
    fun provideSchedulerFacade(): SchedulerFacade {
        return AppSchedulerFacade()
    }

}

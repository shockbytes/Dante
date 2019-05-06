package at.shockbytes.dante.dagger

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.backup.BackupRepository
import at.shockbytes.dante.backup.DefaultBackupRepository
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.backup.provider.GoogleDriveBackupProvider
import at.shockbytes.dante.backup.provider.ShockbytesHerokuServerBackupProvider
import at.shockbytes.dante.book.BookSuggestion
import at.shockbytes.dante.book.realm.RealmInstanceProvider
import at.shockbytes.dante.network.google.gson.GoogleBooksSuggestionResponseDeserializer
import at.shockbytes.dante.signin.GoogleSignInManager
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.ui.image.GlideImageLoader
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.ui.image.ImagePicker
import at.shockbytes.dante.ui.image.RxLegacyImagePicker
import at.shockbytes.dante.util.DanteRealmMigration
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.flagging.FeatureFlagging
import at.shockbytes.dante.util.flagging.FirebaseFeatureFlagging
import at.shockbytes.dante.util.flagging.SharedPreferencesFeatureFlagging
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
import dagger.Reusable
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
    fun provideRealmInstanceProvider(): RealmInstanceProvider {
        return RealmInstanceProvider(RealmConfiguration.Builder()
                .schemaVersion(DanteRealmMigration.migrationVersion)
                .migration(DanteRealmMigration())
                .build())
    }

    @Provides
    @Reusable
    fun provideBackupRepository(
        backupProvider: Array<BackupProvider>,
        preferences: SharedPreferences
    ): BackupRepository {
        return DefaultBackupRepository(backupProvider.toList(), preferences)
    }

    @Provides
    @Reusable
    fun provideBackupProvider(
        schedulerFacade: SchedulerFacade,
        signInManager: SignInManager
    ): Array<BackupProvider> {
        return arrayOf(
            GoogleDriveBackupProvider(
                signInManager as GoogleSignInManager,
                schedulerFacade,
                Gson()
            ),
            ShockbytesHerokuServerBackupProvider()
        )
    }

    @Provides
    @Singleton
    fun provideGoogleSignInManager(prefs: SharedPreferences): SignInManager {
        return GoogleSignInManager(prefs, app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideFeatureFlagging(remoteConfig: FirebaseRemoteConfig): FeatureFlagging {
        return if (BuildConfig.DEBUG) {
            val prefs = app.getSharedPreferences("feature_flagging", Context.MODE_PRIVATE)
            SharedPreferencesFeatureFlagging(prefs)
        } else {
            FirebaseFeatureFlagging(remoteConfig)
        }
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

    @Provides
    @Singleton
    fun provideImagePicker(): ImagePicker {
        return RxLegacyImagePicker()
    }
}

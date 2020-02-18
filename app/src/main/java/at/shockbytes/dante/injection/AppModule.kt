package at.shockbytes.dante.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.announcement.SharedPrefsAnnouncementProvider
import at.shockbytes.dante.backup.BackupRepository
import at.shockbytes.dante.backup.DefaultBackupRepository
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.storage.DefaultExternalStorageInteractor
import at.shockbytes.dante.backup.provider.external.ExternalStorageBackupProvider
import at.shockbytes.dante.storage.ExternalStorageInteractor
import at.shockbytes.dante.backup.provider.google.GoogleDriveBackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.ShockbytesHerokuServerBackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.api.ShockbytesHerokuApi
import at.shockbytes.dante.backup.provider.shockbytes.storage.InactiveShockbytesBackupStorage
import at.shockbytes.dante.backup.provider.shockbytes.storage.SharedPreferencesInactiveShockbytesBackupStorage
import at.shockbytes.dante.signin.GoogleSignInManager
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.flagging.FeatureFlagging
import at.shockbytes.dante.flagging.FirebaseFeatureFlagging
import at.shockbytes.dante.flagging.SharedPreferencesFeatureFlagging
import at.shockbytes.dante.util.permission.AndroidPermissionManager
import at.shockbytes.dante.util.permission.PermissionManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.tracking.DebugTracker
import at.shockbytes.tracking.FirebaseTracker
import at.shockbytes.tracking.Tracker
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import dagger.Module
import dagger.Provides

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
@Module
class AppModule(private val app: Application) {

    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
    }

    @Provides
    fun provideDanteSettings(sharedPreferences: SharedPreferences): DanteSettings {
        return DanteSettings(app.applicationContext, sharedPreferences)
    }

    @Provides
    fun provideInactiveShockbytesBackupStorage(
        preferences: SharedPreferences
    ): InactiveShockbytesBackupStorage {
        return SharedPreferencesInactiveShockbytesBackupStorage(preferences)
    }

    @Provides
    fun provideBackupRepository(
        backupProvider: Array<BackupProvider>,
        preferences: SharedPreferences
    ): BackupRepository {
        return DefaultBackupRepository(backupProvider.toList(), preferences)
    }

    @Provides
    fun provideExternalStorageInteractor(): ExternalStorageInteractor {
        return DefaultExternalStorageInteractor(app.applicationContext)
    }

    @Provides
    fun providePermissionManager(): PermissionManager {
        return AndroidPermissionManager()
    }

    @Provides
    fun provideBackupProvider(
        schedulerFacade: SchedulerFacade,
        signInManager: SignInManager,
        shockbytesHerokuApi: ShockbytesHerokuApi,
        inactiveShockbytesBackupStorage: InactiveShockbytesBackupStorage,
        externalStorageInteractor: ExternalStorageInteractor,
        permissionManager: PermissionManager
    ): Array<BackupProvider> {
        return arrayOf(
            GoogleDriveBackupProvider(
                signInManager as GoogleSignInManager,
                schedulerFacade,
                Gson()
            ),
            ShockbytesHerokuServerBackupProvider(
                signInManager,
                shockbytesHerokuApi,
                inactiveShockbytesBackupStorage
            ),
            ExternalStorageBackupProvider(
                schedulerFacade,
                Gson(),
                externalStorageInteractor,
                permissionManager
            )
        )
    }

    @Provides
    fun provideGoogleSignInManager(prefs: SharedPreferences): SignInManager {
        return GoogleSignInManager(prefs, app.applicationContext)
    }

    @Provides
    fun provideFeatureFlagging(remoteConfig: FirebaseRemoteConfig): FeatureFlagging {
        return if (BuildConfig.DEBUG) {
            val prefs = app.getSharedPreferences("feature_flagging", Context.MODE_PRIVATE)
            SharedPreferencesFeatureFlagging(prefs)
        } else {
            FirebaseFeatureFlagging(remoteConfig)
        }
    }

    @Provides
    fun provideAnnouncementProvider(): AnnouncementProvider {
        val prefs = app.getSharedPreferences("announcements", Context.MODE_PRIVATE)
        return SharedPrefsAnnouncementProvider(prefs)
    }

    @Provides
    fun provideTracker(): Tracker {
        return if (BuildConfig.DEBUG) {
            DebugTracker()
        } else {
            FirebaseTracker(app.applicationContext)
        }
    }
}

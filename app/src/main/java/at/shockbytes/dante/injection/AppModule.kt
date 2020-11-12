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
import at.shockbytes.dante.backup.provider.csv.LocalCsvBackupProvider
import at.shockbytes.dante.storage.DefaultExternalStorageInteractor
import at.shockbytes.dante.backup.provider.external.ExternalStorageBackupProvider
import at.shockbytes.dante.backup.provider.google.DriveClient
import at.shockbytes.dante.backup.provider.google.DriveRestClient
import at.shockbytes.dante.storage.ExternalStorageInteractor
import at.shockbytes.dante.backup.provider.google.GoogleDriveBackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.ShockbytesHerokuServerBackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.api.ShockbytesHerokuApi
import at.shockbytes.dante.backup.provider.shockbytes.storage.InactiveShockbytesBackupStorage
import at.shockbytes.dante.backup.provider.shockbytes.storage.SharedPreferencesInactiveShockbytesBackupStorage
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.signin.GoogleFirebaseSignInManager
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.flagging.FeatureFlagging
import at.shockbytes.dante.flagging.FirebaseFeatureFlagging
import at.shockbytes.dante.flagging.SharedPreferencesFeatureFlagging
import at.shockbytes.dante.importer.DanteCsvImportProvider
import at.shockbytes.dante.importer.DanteExternalStorageImportProvider
import at.shockbytes.dante.importer.DefaultImportRepository
import at.shockbytes.dante.importer.GoodreadsCsvImportProvider
import at.shockbytes.dante.importer.ImportProvider
import at.shockbytes.dante.importer.ImportRepository
import at.shockbytes.dante.storage.reader.CsvReader
import at.shockbytes.dante.util.permission.AndroidPermissionManager
import at.shockbytes.dante.util.permission.PermissionManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
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
    fun provideDanteSettings(
        sharedPreferences: SharedPreferences,
        schedulers: SchedulerFacade
    ): DanteSettings {
        return DanteSettings(app.applicationContext, sharedPreferences, schedulers)
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
        preferences: SharedPreferences,
        tracker: Tracker
    ): BackupRepository {
        return DefaultBackupRepository(backupProvider.toList(), preferences, tracker)
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
    fun provideDriveClient(signInManager: SignInManager): DriveClient {
        return DriveRestClient(signInManager as GoogleFirebaseSignInManager)
    }

    @Provides
    fun provideBackupProvider(
        schedulerFacade: SchedulerFacade,
        signInManager: SignInManager,
        shockbytesHerokuApi: ShockbytesHerokuApi,
        inactiveShockbytesBackupStorage: InactiveShockbytesBackupStorage,
        externalStorageInteractor: ExternalStorageInteractor,
        permissionManager: PermissionManager,
        csvImportProvider: DanteCsvImportProvider,
        driveClient: DriveClient,
        danteExternalStorageImportProvider: DanteExternalStorageImportProvider
    ): Array<BackupProvider> {
        return arrayOf(
            GoogleDriveBackupProvider(
                schedulerFacade,
                Gson(),
                driveClient
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
                permissionManager,
                danteExternalStorageImportProvider
            ),
            LocalCsvBackupProvider(
                schedulerFacade,
                externalStorageInteractor,
                permissionManager,
                csvImportProvider
            )
        )
    }

    @Provides
    fun provideGoogleSignInManager(
        prefs: SharedPreferences,
        schedulers: SchedulerFacade
    ): SignInManager {
        return GoogleFirebaseSignInManager(prefs, app.applicationContext, schedulers)
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
    fun provideDanteCsvImportProvider(schedulers: SchedulerFacade): DanteCsvImportProvider {
        return DanteCsvImportProvider(CsvReader(), schedulers)
    }

    @Provides
    fun provideDanteExternalStorageImportProvider(): DanteExternalStorageImportProvider {
        return DanteExternalStorageImportProvider(gson = Gson())
    }

    @Provides
    fun provideImportProvider(
        schedulers: SchedulerFacade,
        danteCsvImportProvider: DanteCsvImportProvider,
        danteExternalStorageImportProvider: DanteExternalStorageImportProvider
    ): Array<ImportProvider> {
        return arrayOf(
            GoodreadsCsvImportProvider(CsvReader(), schedulers),
            danteCsvImportProvider,
            danteExternalStorageImportProvider
        )
    }

    @Provides
    fun provideImportRepository(
        importProvider: Array<ImportProvider>,
        bookRepository: BookRepository,
        schedulers: SchedulerFacade
    ): ImportRepository {
        return DefaultImportRepository(importProvider, bookRepository, schedulers)
    }
}

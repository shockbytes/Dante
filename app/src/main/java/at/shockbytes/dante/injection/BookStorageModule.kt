package at.shockbytes.dante.injection

import android.app.Application
import android.content.SharedPreferences
import at.shockbytes.dante.backup.BackupRepository
import at.shockbytes.dante.backup.DefaultBackupRepository
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.backup.provider.csv.LocalCsvBackupProvider
import at.shockbytes.dante.backup.provider.external.ExternalStorageBackupProvider
import at.shockbytes.dante.backup.provider.google.DriveClient
import at.shockbytes.dante.backup.provider.google.DriveRestClient
import at.shockbytes.dante.backup.provider.google.GoogleDriveBackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.ShockbytesHerokuServerBackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.api.ShockbytesHerokuApi
import at.shockbytes.dante.backup.provider.shockbytes.storage.InactiveShockbytesBackupStorage
import at.shockbytes.dante.backup.provider.shockbytes.storage.SharedPreferencesInactiveShockbytesBackupStorage
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.importer.DanteCsvImportProvider
import at.shockbytes.dante.importer.DanteExternalStorageImportProvider
import at.shockbytes.dante.importer.DefaultImportRepository
import at.shockbytes.dante.importer.GoodreadsCsvImportProvider
import at.shockbytes.dante.importer.ImportProvider
import at.shockbytes.dante.importer.ImportRepository
import at.shockbytes.dante.core.login.GoogleFirebaseLoginRepository
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.storage.DefaultExternalStorageInteractor
import at.shockbytes.dante.storage.ExternalStorageInteractor
import at.shockbytes.dante.storage.reader.CsvReader
import at.shockbytes.dante.util.permission.PermissionManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.tracking.Tracker
import com.google.gson.Gson
import dagger.Module
import dagger.Provides

@Module
class BookStorageModule(private val app: Application) {

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
    fun provideDriveClient(loginRepository: LoginRepository): DriveClient {
        return DriveRestClient(loginRepository as GoogleFirebaseLoginRepository)
    }

    @Provides
    fun provideBackupProvider(
        schedulerFacade: SchedulerFacade,
        loginRepository: LoginRepository,
        shockbytesHerokuApi: ShockbytesHerokuApi,
        inactiveShockbytesBackupStorage: InactiveShockbytesBackupStorage,
        externalStorageInteractor: ExternalStorageInteractor,
        permissionManager: PermissionManager,
        csvImportProvider: DanteCsvImportProvider,
        driveClient: DriveClient
    ): Array<BackupProvider> {
        return arrayOf(
            GoogleDriveBackupProvider(
                schedulerFacade,
                driveClient
            ),
            ShockbytesHerokuServerBackupProvider(
                loginRepository,
                shockbytesHerokuApi,
                inactiveShockbytesBackupStorage
            ),
            ExternalStorageBackupProvider(
                schedulerFacade,
                Gson(),
                externalStorageInteractor,
                permissionManager
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
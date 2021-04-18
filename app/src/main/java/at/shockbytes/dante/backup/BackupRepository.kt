package at.shockbytes.dante.backup

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupContent
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.util.RestoreStrategy
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.data.PageRecordDao
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Author:  Martin Macheiner
 * Date:    06.05.2019
 */
interface BackupRepository {

    val backupProvider: List<BackupProvider>

    fun setLastBackupTime(timeInMillis: Long)

    fun observeLastBackupTime(): Observable<Long>

    fun getBackups(): Single<List<BackupMetadataState>>

    fun initialize(activity: FragmentActivity, forceReload: Boolean): Completable

    fun close(): Completable

    fun removeBackupEntry(entry: BackupMetadata): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(
        backupContent: BackupContent,
        backupStorageProvider: BackupStorageProvider
    ): Completable

    fun restoreBackup(
        entry: BackupMetadata,
        bookRepository: BookRepository,
        pageRecordDao: PageRecordDao,
        strategy: RestoreStrategy
    ): Completable
}
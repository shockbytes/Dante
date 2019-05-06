package at.shockbytes.dante.backup

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.model.RestoreStrategy
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    06.05.2019
 */
interface BackupRepository {

    val backupProvider: List<BackupProvider>

    var lastBackupTime: Long

    fun getBackups(): Single<List<BackupEntryState>>

    @Throws(BackupServiceConnectionException::class)
    fun initialize(activity: FragmentActivity): Completable

    fun close(): Completable

    fun removeBackupEntry(entry: BackupEntry): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(books: List<BookEntity>, backupStorageProvider: BackupStorageProvider): Completable

    fun restoreBackup(
        entry: BackupEntry,
        bookDao: BookEntityDao,
        strategy: RestoreStrategy
    ): Completable

    companion object {
        const val KEY_LAST_BACKUP = "key_last_backup"
    }

}
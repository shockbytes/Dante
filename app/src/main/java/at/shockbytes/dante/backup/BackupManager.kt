package at.shockbytes.dante.backup

import android.support.v4.app.FragmentActivity
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import io.reactivex.Completable
import io.reactivex.Single

/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */

interface BackupManager {

    enum class RestoreStrategy {
        MERGE, OVERWRITE
    }

    var lastBackupTime: Long

    val backupList: Single<List<BackupEntry>>

    @Throws(BackupServiceConnectionException::class)
    fun connect(activity: FragmentActivity): Completable

    fun close(books: List<BookEntity>? = null)

    fun removeBackupEntry(entry: BackupEntry): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(books: List<BookEntity>): Completable

    fun restoreBackup(
        entry: BackupEntry,
        bookDao: BookEntityDao,
        strategy: RestoreStrategy
    ): Completable
}

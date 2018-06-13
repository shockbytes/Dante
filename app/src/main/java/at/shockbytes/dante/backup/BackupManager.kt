package at.shockbytes.dante.backup

import android.support.v4.app.FragmentActivity
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */

interface BackupManager {

    enum class RestoreStrategy {
        MERGE, OVERWRITE
    }

    val lastBackupTime: Long

    val backupList: Single<List<BackupEntry>>
    
    fun connect(activity: FragmentActivity)

    fun close(books: List<BookEntity>? = null)

    fun removeBackupEntry(entry: BackupEntry): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(booksObservable: Flowable<List<BookEntity>>): Completable

    fun restoreBackup(entry: BackupEntry, bookDao: BookEntityDao,
                      strategy: RestoreStrategy): Completable

}

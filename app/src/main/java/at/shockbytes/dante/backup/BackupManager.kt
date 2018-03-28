package at.shockbytes.dante.backup

import android.support.v4.app.FragmentActivity
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.util.books.Book
import io.reactivex.Completable
import io.reactivex.Observable
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

    fun close(books: List<Book>? = null)

    fun removeBackupEntry(entry: BackupEntry): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(booksObservable: Observable<List<Book>>): Completable

    fun restoreBackup(entry: BackupEntry, bookManager: BookManager,
                      strategy: RestoreStrategy): Completable

}

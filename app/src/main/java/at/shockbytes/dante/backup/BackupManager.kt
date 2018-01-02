package at.shockbytes.dante.backup

import android.support.v4.app.FragmentActivity

import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.books.BookManager
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */

interface BackupManager {

    enum class RestoreStrategy {
        MERGE, OVERWRITE
    }
    
    var isAutoBackupEnabled: Boolean

    val lastBackupTime: Long

    val backupList: Observable<List<BackupEntry>>
    
    fun connect(activity: FragmentActivity)

    fun close(books: List<Book>? = null)

    fun removeBackupEntry(entry: BackupEntry): Completable

    fun removeAllBackupEntries(): Completable

    fun backup(books: List<Book>): Completable

    fun restoreBackup(entry: BackupEntry, bookManager: BookManager,
                      strategy: RestoreStrategy): Completable

}

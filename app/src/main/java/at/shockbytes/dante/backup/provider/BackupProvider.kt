package at.shockbytes.dante.backup.provider

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single

interface BackupProvider {

    val backupStorageProvider: BackupStorageProvider

    var isEnabled: Boolean

    fun initialize(activity: FragmentActivity? = null): Completable

    fun backup(books: List<BookEntity>): Completable

    fun getBackupEntries(): Single<List<BackupEntryState>>

    fun removeBackupEntry(entry: BackupEntry): Completable

    fun removeAllBackupEntries(): Completable

    fun mapEntryToBooks(entry: BackupEntry): Single<List<BookEntity>>

    fun teardown(): Completable
}
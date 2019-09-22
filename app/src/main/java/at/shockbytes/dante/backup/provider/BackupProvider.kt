package at.shockbytes.dante.backup.provider

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single

interface BackupProvider {

    val backupStorageProvider: BackupStorageProvider

    var isEnabled: Boolean

    fun initialize(activity: FragmentActivity? = null): Completable

    fun backup(books: List<BookEntity>): Completable

    fun getBackupEntries(): Single<List<BackupMetadataState>>

    fun removeBackupEntry(entry: BackupMetadata): Completable

    fun removeAllBackupEntries(): Completable

    fun mapEntryToBooks(entry: BackupMetadata): Single<List<BookEntity>>

    fun teardown(): Completable
}
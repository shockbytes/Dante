package at.shockbytes.dante.backup.provider.google

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single

class GoogleDriveRestBackupProvider : BackupProvider {

    override val backupStorageProvider: BackupStorageProvider = BackupStorageProvider.GOOGLE_DRIVE
    override var isEnabled: Boolean = true

    override fun initialize(activity: FragmentActivity?): Completable {
        TODO("Not yet implemented")
    }

    override fun backup(books: List<BookEntity>): Completable {
        TODO("Not yet implemented")
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        TODO("Not yet implemented")
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        TODO("Not yet implemented")
    }

    override fun removeAllBackupEntries(): Completable {
        TODO("Not yet implemented")
    }

    override fun mapEntryToBooks(entry: BackupMetadata): Single<List<BookEntity>> {
        TODO("Not yet implemented")
    }

    override fun teardown(): Completable {
        TODO("Not yet implemented")
    }
}
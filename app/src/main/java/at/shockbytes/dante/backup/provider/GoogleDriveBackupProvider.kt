package at.shockbytes.dante.backup.provider

import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single

class GoogleDriveBackupProvider : BackupProvider {

    override val backupStorageProvider: BackupStorageProvider = BackupStorageProvider.GOOGLE_DRIVE

    override fun mapEntryToBooks(entry: BackupEntry): Single<List<BookEntity>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun backup(books: List<BookEntity>): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun initialize(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun teardown(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBackupEntries(): Single<List<BackupEntryState>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeBackupEntry(entry: BackupEntry): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAllBackupEntries(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
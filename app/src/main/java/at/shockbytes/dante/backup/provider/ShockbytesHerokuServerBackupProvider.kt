package at.shockbytes.dante.backup.provider

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single

class ShockbytesHerokuServerBackupProvider : BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.SHOCKBYTES_SERVER

    override fun initialize(activity: FragmentActivity?): Completable {
        // TODO: Initialize provider
        return Completable.complete()
    }

    override fun backup(books: List<BookEntity>): Completable {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getBackupEntries(): Single<List<BackupEntryState>> {
        return Single.just(listOf(
            BackupEntryState.Active(
                BackupEntry(
                    id = "id1",
                    fileName = "file_name_1",
                    device = "Nexus 5",
                    storageProvider = BackupStorageProvider.SHOCKBYTES_SERVER,
                    books = 100,
                    timestamp = System.currentTimeMillis()
                )
            ),
            BackupEntryState.Inactive(
                BackupEntry(
                    id = "id3",
                    fileName = "file_name_3",
                    device = "Sony Ericsson X10 Mini",
                    storageProvider = BackupStorageProvider.SHOCKBYTES_SERVER,
                    books = 10,
                    timestamp = 1546810565000L
                )
            ),
            BackupEntryState.Inactive(
                BackupEntry(
                    id = "id2",
                    fileName = "file_name_2",
                    device = "Nexus 4",
                    storageProvider = BackupStorageProvider.SHOCKBYTES_SERVER,
                    books = 40,
                    timestamp = 1430948165000L
                )
            )
        ))
    }

    override fun removeBackupEntry(entry: BackupEntry): Completable {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAllBackupEntries(): Completable {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun mapEntryToBooks(entry: BackupEntry): Single<List<BookEntity>> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }
}
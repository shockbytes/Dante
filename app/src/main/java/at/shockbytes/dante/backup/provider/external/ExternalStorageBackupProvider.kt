package at.shockbytes.dante.backup.provider.external

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    28.05.2019
 */
class ExternalStorageBackupProvider(
    private val schedulers: SchedulerFacade,
    private val gson: Gson
) : BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.EXTERNAL_STORAGE

    override fun initialize(activity: FragmentActivity?): Completable {
        // TODO Implement this method...
        return Completable.complete()
    }

    override fun backup(books: List<BookEntity>): Completable {
        // TODO Implement this method...
        return Completable.complete()
    }

    override fun getBackupEntries(): Single<List<BackupEntryState>> {
        // TODO Implement this method...
        return Single.just(
            listOf(
                BackupEntryState.Active(
                    BackupEntry(
                        id = "id1",
                        fileName = "filename1",
                        device = "Nexus 4",
                        storageProvider = backupStorageProvider,
                        books = 64,
                        timestamp = System.currentTimeMillis()
                    )
                ),
                BackupEntryState.Inactive(
                    BackupEntry(
                        id = "id2",
                        fileName = "filename2",
                        device = "Nexus 7",
                        storageProvider = backupStorageProvider,
                        books = 128,
                        timestamp = System.currentTimeMillis() - 100000000L
                    )
                )
            )
        )
    }

    override fun removeBackupEntry(entry: BackupEntry): Completable {
        // TODO Implement this method...
        return Completable.complete()
    }

    override fun removeAllBackupEntries(): Completable {
        // TODO Implement this method...
        return Completable.complete()
    }

    override fun mapEntryToBooks(entry: BackupEntry): Single<List<BookEntity>> {
        // TODO Implement this method...
        return Single.just(listOf())
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }
}
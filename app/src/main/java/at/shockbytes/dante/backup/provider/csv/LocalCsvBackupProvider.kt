package at.shockbytes.dante.backup.provider.csv

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Single

class LocalCsvBackupProvider(
    private val schedulerFacade: SchedulerFacade
): BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.LOCAL_CSV
    override var isEnabled: Boolean = true

    override fun initialize(activity: FragmentActivity?): Completable {
        return Completable.complete()
    }

    override fun backup(books: List<BookEntity>): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAllBackupEntries(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mapEntryToBooks(entry: BackupMetadata): Single<List<BookEntity>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }
}
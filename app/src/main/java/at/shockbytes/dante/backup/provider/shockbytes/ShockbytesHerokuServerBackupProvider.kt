package at.shockbytes.dante.backup.provider.shockbytes

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.api.ShockbytesHerokuApi
import at.shockbytes.dante.backup.provider.shockbytes.storage.InactiveShockbytesBackupStorage
import at.shockbytes.dante.book.BookEntity
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ShockbytesHerokuServerBackupProvider(
    private val shockbytesHerokuApi: ShockbytesHerokuApi,
    private val inactiveBackupStorage: InactiveShockbytesBackupStorage
) : BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.SHOCKBYTES_SERVER

    override fun initialize(activity: FragmentActivity?): Completable {
        // TODO: Initialize provider
        return Completable.complete()
    }

    override fun backup(books: List<BookEntity>): Completable {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getBackupEntries(): Single<List<BackupEntryState>> {
        return shockbytesHerokuApi.listBackups("this is just a test token")
            .map { entries ->
                val entryStates: List<BackupEntryState> = entries.map { entry ->
                    BackupEntryState.Active(entry)
                }
                entryStates
            }
            .subscribeOn(Schedulers.io())
            .doOnSuccess { activeItems ->
                inactiveBackupStorage.storeInactiveItems(activeItems)
            }
            .onErrorReturn { throwable ->
                Timber.e(throwable)
                inactiveBackupStorage.getInactiveItems()
            }
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
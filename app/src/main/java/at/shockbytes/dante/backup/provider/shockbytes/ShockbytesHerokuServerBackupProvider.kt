package at.shockbytes.dante.backup.provider.shockbytes

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.api.ShockbytesHerokuApi
import at.shockbytes.dante.backup.provider.shockbytes.storage.InactiveShockbytesBackupStorage
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.signin.GoogleSignInManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ShockbytesHerokuServerBackupProvider(
    private val signInManager: GoogleSignInManager,
    private val shockbytesHerokuApi: ShockbytesHerokuApi,
    private val inactiveBackupStorage: InactiveShockbytesBackupStorage
) : BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.SHOCKBYTES_SERVER

    override fun initialize(activity: FragmentActivity?): Completable {
        // TODO: Initialize provider
        return Completable.complete()
    }

    override fun backup(books: List<BookEntity>): Completable {
        return shockbytesHerokuApi
            .makeBackup(signInManager.getAuthorizationHeader(), books)
            .flatMapCompletable { entry ->

                // TODO What to do with entry? Store in UI?
                Completable.complete()
            }
    }

    override fun getBackupEntries(): Single<List<BackupEntryState>> {
        return shockbytesHerokuApi.listBackups(signInManager.getAuthorizationHeader())
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
        return shockbytesHerokuApi.removeBackupById(signInManager.getAuthorizationHeader(), entry.id)
    }

    override fun removeAllBackupEntries(): Completable {
        return shockbytesHerokuApi.removeAllBackups(signInManager.getAuthorizationHeader())
    }

    override fun mapEntryToBooks(entry: BackupEntry): Single<List<BookEntity>> {
        return shockbytesHerokuApi
            .getBooksBackupById(signInManager.getAuthorizationHeader(), entry.id)
            .subscribeOn(Schedulers.io())
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }
}
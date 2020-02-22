package at.shockbytes.dante.backup.provider.shockbytes

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.backup.provider.shockbytes.api.ShockbytesHerokuApi
import at.shockbytes.dante.backup.provider.shockbytes.storage.InactiveShockbytesBackupStorage
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.signin.GoogleFirebaseSignInManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    09.05.2019
 */
class ShockbytesHerokuServerBackupProvider(
    private val signInManager: GoogleFirebaseSignInManager,
    private val shockbytesHerokuApi: ShockbytesHerokuApi,
    private val inactiveBackupStorage: InactiveShockbytesBackupStorage
) : BackupProvider {

    // Enable it only in debug mode
    override var isEnabled: Boolean = BuildConfig.DEBUG

    override val backupStorageProvider = BackupStorageProvider.SHOCKBYTES_SERVER

    override fun initialize(activity: FragmentActivity?): Completable {
        // TODO: Initialize provider
        return Completable.complete()
    }

    override fun backup(books: List<BookEntity>): Completable {
        return signInManager.getAuthorizationHeader()
            .flatMapCompletable { token ->
            shockbytesHerokuApi.makeBackup(token, books)
                .flatMapCompletable { entry ->
                    Timber.d(entry.toString())
                    // TODO What to do with entry? Store in UI?
                    Completable.complete()
                }
        }
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        return signInManager.getAuthorizationHeader()
            .flatMap { token ->
                shockbytesHerokuApi.listBackups(token)
                    .map { entries ->
                        val entryStates: List<BackupMetadataState> = entries.map { entry ->
                            BackupMetadataState.Active(entry)
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
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return signInManager.getAuthorizationHeader()
            .flatMapCompletable { token ->
                shockbytesHerokuApi.removeBackupById(token, entry.id)
            }
    }

    override fun removeAllBackupEntries(): Completable {
        return signInManager.getAuthorizationHeader()
            .flatMapCompletable(shockbytesHerokuApi::removeAllBackups)
    }

    override fun mapEntryToBooks(entry: BackupMetadata): Single<List<BookEntity>> {
        return signInManager.getAuthorizationHeader()
            .flatMap { token ->
                shockbytesHerokuApi
                    .getBooksBackupById(token, entry.id)
                    .subscribeOn(Schedulers.io())
            }
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }
}
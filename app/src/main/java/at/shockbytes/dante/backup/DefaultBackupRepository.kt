package at.shockbytes.dante.backup

import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.model.RestoreStrategy
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.backup.model.BackupStorageProviderNotAvailableException
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.settings.delegate.SharedPreferencesLongPropertyDelegate
import io.reactivex.Completable
import io.reactivex.Single

class DefaultBackupRepository(
    override val backupProvider: List<BackupProvider>,
    preferences: SharedPreferences
) : BackupRepository {

    private val activeBackupProvider: List<BackupProvider>
        get() = backupProvider.filter { it.isEnabled }

    override var lastBackupTime: Long by SharedPreferencesLongPropertyDelegate(preferences, BackupRepository.KEY_LAST_BACKUP, 0)

    override fun getBackups(): Single<List<BackupMetadataState>> {

        return Single.merge(activeBackupProvider.map { it.getBackupEntries() })
            .collect(
                { mutableListOf() },
                { container: MutableList<BackupMetadataState>, value: List<BackupMetadataState> ->
                    container.addAll(value)
                }
            )
            .map { entries ->
                entries
                    .sortedByDescending {
                        it.timestamp
                    }
                    .toList()
            }
    }

    override fun initialize(activity: FragmentActivity, forceReload: Boolean): Completable {

        // If forceReload is set, then use the whole list of backup provider,
        // otherwise just use the active ones
        val provider = if (forceReload) backupProvider else activeBackupProvider

        return Completable.concat(provider.map { it.initialize(activity) })
    }

    override fun close(): Completable {
        return Completable.concat(activeBackupProvider.map { it.teardown() })
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return getBackupProvider(entry.storageProvider)?.removeBackupEntry(entry)
            ?: Completable.error(BackupStorageProviderNotAvailableException())
    }

    override fun removeAllBackupEntries(): Completable {
        return Completable.concat(activeBackupProvider.map { it.removeAllBackupEntries() })
    }

    override fun backup(books: List<BookEntity>, backupStorageProvider: BackupStorageProvider): Completable {
        return getBackupProvider(backupStorageProvider)
            ?.backup(books)
            ?.doOnComplete {
                lastBackupTime = System.currentTimeMillis()
            }
            ?: Completable.error(BackupStorageProviderNotAvailableException())
    }

    override fun restoreBackup(
        entry: BackupMetadata,
        bookDao: BookEntityDao,
        strategy: RestoreStrategy
    ): Completable {

        return getBackupProvider(entry.storageProvider)?.mapEntryToBooks(entry)
            ?.flatMapCompletable { books ->
                bookDao.restoreBackup(books, strategy)
            } ?: Completable.error(BackupStorageProviderNotAvailableException())
    }

    private fun getBackupProvider(source: BackupStorageProvider): BackupProvider? {
        return activeBackupProvider.find { it.backupStorageProvider == source }
    }
}
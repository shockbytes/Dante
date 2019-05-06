package at.shockbytes.dante.backup

import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.model.RestoreStrategy
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.backup.model.BackupStorageProviderNotAvailableException
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.settings.delegate.SharedPreferencesLongPropertyDelegate
import io.reactivex.Completable
import io.reactivex.Single

class DefaultBackupRepository(
    override val backupProvider: List<BackupProvider>,
    preferences: SharedPreferences
) : BackupRepository {

    override var lastBackupTime: Long by SharedPreferencesLongPropertyDelegate(preferences, BackupRepository.KEY_LAST_BACKUP, 0)

    override fun getBackups(): Single<List<BackupEntryState>> {

        return Single.merge(backupProvider.map { it.getBackupEntries() })
            .collect(
                { mutableListOf() },
                { container: MutableList<BackupEntryState>, value: List<BackupEntryState> ->
                    container.addAll(value)
                }
            )
            .map { entries ->
                entries
                    .sortedByDescending {
                        it.entry.timestamp
                    }
                    .toList()
            }
    }

    override fun initialize(activity: FragmentActivity): Completable {
        return Completable.concat(backupProvider.map { it.initialize(activity) })
    }

    override fun close(): Completable {
        return Completable.concat(backupProvider.map { it.teardown() })
    }

    override fun removeBackupEntry(entry: BackupEntry): Completable {
        return getBackupProvider(entry.storageProvider)?.removeBackupEntry(entry)
            ?: Completable.error(BackupStorageProviderNotAvailableException())
    }

    override fun removeAllBackupEntries(): Completable {
        return Completable.concat(backupProvider.map { it.removeAllBackupEntries() })
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
        entry: BackupEntry,
        bookDao: BookEntityDao,
        strategy: RestoreStrategy
    ): Completable {

        return getBackupProvider(entry.storageProvider)?.mapEntryToBooks(entry)
            ?.flatMapCompletable { books ->
                bookDao.restoreBackup(books, strategy)
            } ?: Completable.error(BackupStorageProviderNotAvailableException())
    }

    private fun getBackupProvider(source: BackupStorageProvider): BackupProvider? {
        return backupProvider.find { it.backupStorageProvider == source }
    }
}
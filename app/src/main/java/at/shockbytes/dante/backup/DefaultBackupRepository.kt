package at.shockbytes.dante.backup

import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupContent
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.util.RestoreStrategy
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.backup.model.BackupStorageProviderNotAvailableException
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.util.settings.delegate.edit
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class DefaultBackupRepository(
    override val backupProvider: List<BackupProvider>,
    private val preferences: SharedPreferences,
    private val tracker: Tracker
) : BackupRepository {

    private val activeBackupProvider: List<BackupProvider>
        get() = backupProvider.filter { it.isEnabled }

    override fun setLastBackupTime(timeInMillis: Long) {
        preferences.edit {
            putLong(KEY_LAST_BACKUP, timeInMillis)
        }
    }

    override fun observeLastBackupTime(): Observable<Long> {
        return RxSharedPreferences.create(preferences)
            .getLong(KEY_LAST_BACKUP, 0)
            .asObservable()
    }

    override fun getBackups(): Single<List<BackupMetadataState>> {

        val activeBackupProviderSources = activeBackupProvider.map { it.getBackupEntries() }

        return Single.merge(activeBackupProviderSources)
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

        // If forceReload is set, then use the whole listBackupFiles of backup provider,
        // otherwise just use the active ones
        val provider = if (forceReload) backupProvider else activeBackupProvider

        return Completable.concat(provider.map { it.initialize(activity) })
    }

    override fun close(): Completable {
        return Completable.concat(activeBackupProvider.map { it.teardown() })
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return getBackupProvider(entry.storageProvider).removeBackupEntry(entry)
    }

    override fun removeAllBackupEntries(): Completable {
        return Completable.concat(activeBackupProvider.map { it.removeAllBackupEntries() })
    }

    override fun backup(
        backupContent: BackupContent,
        backupStorageProvider: BackupStorageProvider
    ): Completable {
        return getBackupProvider(backupStorageProvider)
            .backup(backupContent)
            .doOnComplete {
                setLastBackupTime(System.currentTimeMillis())
                trackBackupMadeEvent(backupStorageProvider)
            }
            ?: Completable.error(BackupStorageProviderNotAvailableException())
    }

    private fun trackBackupMadeEvent(backupStorageProvider: BackupStorageProvider) {
        tracker.track(DanteTrackingEvent.BackupMadeEvent(backupStorageProvider.acronym))
    }

    override fun restoreBackup(
        entry: BackupMetadata,
        bookRepository: BookRepository,
        pageRecordDao: PageRecordDao,
        strategy: RestoreStrategy
    ): Completable {
        return getBackupProvider(entry.storageProvider)
            .mapBackupToBackupContent(entry)
            .flatMapCompletable { (books, pageRecords) ->
                val copyOfBooks = books.map { it.copy() }
                bookRepository.restoreBackup(books, strategy)
                    .andThen(restorePageRecords(bookRepository, pageRecordDao, books = copyOfBooks, pageRecords, strategy))
            }
    }

    private fun restorePageRecords(
        bookRepository: BookRepository,
        pageRecordDao: PageRecordDao,
        books: List<BookEntity>,
        pageRecords: List<PageRecord>,
        strategy: RestoreStrategy
    ): Completable {
        return bookRepository.bookObservable
            .firstOrError()
            .map { restoredBooks ->
                val map = createIdMappingForRestoredBooks(restoredBooks, books)
                pageRecords.map { pageRecord ->
                    pageRecord.copy(bookId = map[pageRecord.bookId]
                        ?: error("Cannot find previously restored book by map lookup!"))
                }
            }
            .flatMapCompletable { mappedPageRecords ->
                pageRecordDao.restoreBackup(mappedPageRecords, strategy)
            }
    }

    private fun createIdMappingForRestoredBooks(
        restoredBooks: List<BookEntity>,
        backupBooks: List<BookEntity>
    ): Map<Long, Long> {
        return restoredBooks.associate { book ->

            val backupBookId = backupBooks.find {
                book.title == it.title && book.author == it.author
            }?.id ?: throw IllegalStateException("Cannot find previously restored book by title lookup!")

            backupBookId to book.id
        }
    }

    private fun getBackupProvider(source: BackupStorageProvider): BackupProvider {
        return activeBackupProvider.find { it.backupStorageProvider == source }
            ?: throw BackupStorageProviderNotAvailableException()
    }

    companion object {
        private const val KEY_LAST_BACKUP = "key_last_backup"
    }
}
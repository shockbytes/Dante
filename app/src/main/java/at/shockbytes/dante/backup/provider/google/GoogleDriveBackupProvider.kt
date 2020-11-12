package at.shockbytes.dante.backup.provider.google

import android.os.Build
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupException
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber

class GoogleDriveBackupProvider(
    private val schedulers: SchedulerFacade,
    private val gson: Gson,
    private val driveClient: DriveClient
) : BackupProvider {

    override var isEnabled: Boolean = true

    override val backupStorageProvider = BackupStorageProvider.GOOGLE_DRIVE

    override fun mapEntryToBooks(entry: BackupMetadata): Single<List<BookEntity>> {
        return driveClient.readFileAsString(entry.id)
            .map(::fileContentToBooks)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    private fun fileContentToBooks(content: String): List<BookEntity> {
        return gson.fromJson(content, object : TypeToken<List<BookEntity>>() {}.type)
    }

    override fun backup(books: List<BookEntity>): Completable {

        if (books.isEmpty()) {
            return Completable.error(BackupException("No books to backup"))
        }

        val content = gson.toJson(books)
        val filename = createFilename(books.size)

        return driveClient.createFile(filename, content)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    private fun createFilename(books: Int): String {

        val timestamp = System.currentTimeMillis()
        val type = "man"

        return backupStorageProvider.acronym + "_" +
            type + "_" +
            timestamp + "_" +
            books + "_" +
            Build.MODEL + ".json"
    }

    override fun initialize(activity: FragmentActivity?): Completable {

        if (activity == null) {
            isEnabled = false
            throw BackupServiceConnectionException("This backup provider requires an attached activity!")
        }

        return driveClient.initialize(activity)
            .doOnError { isEnabled = false }
            .doOnComplete { isEnabled = true }
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        return driveClient.listBackupFiles()
            .map { entries ->
                entries
                    .mapTo(mutableListOf<BackupMetadataState>()) { BackupMetadataState.Active(it) }
                    .toList()
            }
            .onErrorReturn { throwable ->
                Timber.e(throwable)
                listOf()
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return driveClient.deleteFile(entry.id, entry.fileName)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    override fun removeAllBackupEntries(): Completable {
        return driveClient.deleteListedFiles()
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }
}
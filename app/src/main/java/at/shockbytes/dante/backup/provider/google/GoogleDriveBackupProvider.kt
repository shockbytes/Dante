package at.shockbytes.dante.backup.provider.google

import android.os.Build
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.BackupContentTransform
import at.shockbytes.dante.backup.model.BackupContent
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupException
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber

class GoogleDriveBackupProvider(
    private val schedulers: SchedulerFacade,
    private val driveClient: DriveClient
) : BackupProvider {

    override var isEnabled: Boolean = true

    override val backupStorageProvider = BackupStorageProvider.GOOGLE_DRIVE

    private val contentTransform = BackupContentTransform(backupStorageProvider, ::createFilename)

    override fun mapBackupToBackupContent(entry: BackupMetadata): Single<BackupContent> {
        return driveClient.readFileAsString(entry.id)
            .flatMap(contentTransform::createBackupContentFromBackupData)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    override fun backup(backupContent: BackupContent): Completable {

        if (backupContent.isEmpty) {
            return Completable.error(BackupException("No books to backup"))
        }

        return contentTransform.createActualBackupData(backupContent)
            .flatMapCompletable { (filename, content) ->
                driveClient.createFile(filename, content)
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    private fun createFilename(timestamp: Long, books: Int): String {
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
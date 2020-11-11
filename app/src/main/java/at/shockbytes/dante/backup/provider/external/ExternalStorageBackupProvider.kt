package at.shockbytes.dante.backup.provider.external

import android.Manifest
import android.os.Build
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupRepository
import at.shockbytes.dante.backup.model.BackupItem
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadata.Companion.attachLocalFile
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.importer.DanteExternalStorageImportProvider
import at.shockbytes.dante.storage.ExternalStorageInteractor
import at.shockbytes.dante.util.permission.PermissionManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.singleOf
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    28.05.2019
 */
class ExternalStorageBackupProvider(
    private val schedulers: SchedulerFacade,
    private val gson: Gson,
    private val externalStorageInteractor: ExternalStorageInteractor,
    private val permissionManager: PermissionManager,
    private val externalStorageImporter: DanteExternalStorageImportProvider
) : BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.EXTERNAL_STORAGE
    override var isEnabled: Boolean = true

    override fun initialize(activity: FragmentActivity?): Completable {
        return Completable.fromAction {

            if (activity == null) {
                isEnabled = false
                throw BackupServiceConnectionException("${this.javaClass.simpleName} requires an activity!")
            }

            checkPermissions(activity)

            // If not enabled --> do nothing, we don't have the right permissions
            if (isEnabled) {

                try {
                    externalStorageInteractor.createBaseDirectory(BASE_DIR_NAME)
                    isEnabled = true
                } catch (e: IllegalStateException) {
                    isEnabled = false
                    throw e // Rethrow exception after disabling backup provider
                }
            }
        }
    }

    override fun backup(books: List<BookEntity>): Completable {
        return getBackupContent(books)
            .flatMapCompletable { (fileName, content) ->
                externalStorageInteractor.writeToFileInDirectory(BASE_DIR_NAME, fileName, content)
            }
            .subscribeOn(schedulers.io)
    }

    /**
     * Returns Pair<FileName, Content>
     */
    private fun getBackupContent(books: List<BookEntity>): Single<Pair<String, String>> {
        return Single.fromCallable {
            val timestamp = System.currentTimeMillis()
            val fileName = createFileName(timestamp)
            val metadata = bundleMetadataForStorage(books.size, fileName, timestamp)

            val content = gson.toJson(BackupItem(metadata, books))

            Pair(fileName, content)
        }
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        return externalStorageInteractor
            .listFilesInDirectory(
                BASE_DIR_NAME,
                filterPredicate = { fileName ->
                    fileName.endsWith(BackupRepository.BACKUP_ITEM_SUFFIX)
                }
            ).map { files ->
                files.mapNotNull { backupFile ->
                    backupFileToBackupEntry(backupFile)
                }
            }
            .subscribeOn(schedulers.io)
    }

    private fun backupFileToBackupEntry(backupFile: File): BackupMetadataState? {

        return try {

            val metadata = externalStorageInteractor.readFileContent(
                BASE_DIR_NAME,
                backupFile.name
            ).let { content ->
                gson.fromJson(content, BackupItem::class.java)
                    .backupMetadata
                    // This line is necessary because the local file path is not stored
                    // within the serialized Json, it's only used when loaded
                    .attachLocalFile(backupFile, mimeType = MIME_TYPE_JSON)
            }

            // Can only be active, ExternalStorageBackupProvider does not support cached states
            BackupMetadataState.Active(metadata)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return externalStorageInteractor
            .deleteFileInDirectory(BASE_DIR_NAME, entry.fileName)
            .subscribeOn(schedulers.io)
    }

    override fun removeAllBackupEntries(): Completable {
        return externalStorageInteractor
            .deleteFilesInDirectory(BASE_DIR_NAME)
            .subscribeOn(schedulers.io)
    }

    override fun mapEntryToBooks(entry: BackupMetadata): Single<List<BookEntity>> {
        return singleOf {
                externalStorageInteractor.readFileContent(BASE_DIR_NAME, entry.fileName)
            }
            .flatMap(externalStorageImporter::importFromContent)
            .subscribeOn(schedulers.io)
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }

    private fun checkPermissions(activity: FragmentActivity) {

        permissionManager.verifyPermissions(activity, REQUIRED_PERMISSIONS).let { hasPermissions ->

            // BackupProvider is enabled if it has permissions to read and write external storage
            isEnabled = hasPermissions

            if (!hasPermissions) {

                permissionManager.requestPermissions(
                    activity,
                    REQUIRED_PERMISSIONS,
                    RC_READ_WRITE_EXT_STORAGE,
                    R.string.external_storage_rationale,
                    R.string.rationale_ask_ok,
                    R.string.rationale_ask_cancel
                )
            }
        }
    }

    private fun bundleMetadataForStorage(
        books: Int,
        fileName: String,
        timestamp: Long
    ): BackupMetadata.Standard {
        return BackupMetadata.Standard(
            id = fileName,
            fileName = fileName,
            timestamp = timestamp,
            books = books,
            storageProvider = backupStorageProvider,
            device = Build.MODEL
        )
    }

    private fun createFileName(timestamp: Long): String {
        return "dante-backup-$timestamp${BackupRepository.BACKUP_ITEM_SUFFIX}"
    }

    companion object {

        private const val BASE_DIR_NAME = "Dante"
        private const val MIME_TYPE_JSON = "application/json"
        private const val RC_READ_WRITE_EXT_STORAGE = 0x5321

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
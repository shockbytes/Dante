package at.shockbytes.dante.backup.provider.csv

import android.Manifest
import android.os.Build
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupContent
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.importer.DanteCsvImportProvider
import at.shockbytes.dante.storage.ExternalStorageInteractor
import at.shockbytes.dante.util.permission.PermissionManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.singleOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import java.io.File

class LocalCsvBackupProvider(
    private val schedulers: SchedulerFacade,
    private val externalStorageInteractor: ExternalStorageInteractor,
    private val permissionManager: PermissionManager,
    private val csvImporter: DanteCsvImportProvider
) : BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.LOCAL_CSV
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

    override fun backup(backupContent: BackupContent): Completable {
        return createBackupDataFromBackupContent(backupContent.books)
            .flatMapCompletable { (fileName, content) ->
                externalStorageInteractor.writeToFileInDirectory(BASE_DIR_NAME, fileName, content)
            }
            .subscribeOn(schedulers.io)
    }

    private data class BackupFileContent(val fileName: String, val content: String)

    private fun createBackupDataFromBackupContent(
        books: List<BookEntity>
    ): Single<BackupFileContent> {
        return Single.fromCallable {
            val timestamp = System.currentTimeMillis()
            val fileName = createFileName(timestamp, books.size)
            val content = createContent(books)

            BackupFileContent(fileName, content)
        }
    }

    private fun createContent(books: List<BookEntity>): String {
        val header = "title,subtitle,author,state,pageCount," +
            "publishedDate,isbn,thumbnailAddress," +
            "googleBooksLink,startDate,endDate,wishlistDate," +
            "language,rating,currentPage,notes,summary,labels\n"
        return header + books.joinToString("\n") { b ->
            listOf(
                checkCsvItem(b.title),
                checkCsvItem(b.subTitle),
                checkCsvItem(b.author),
                checkCsvItem(b.state.name),
                checkCsvItem(b.pageCount.toString()),
                checkCsvItem(b.publishedDate),
                checkCsvItem(b.isbn),
                checkCsvItem(b.thumbnailAddress),
                checkCsvItem(b.googleBooksLink),
                checkCsvItem(b.startDate.toString()),
                checkCsvItem(b.endDate.toString()),
                checkCsvItem(b.wishlistDate.toString()),
                checkCsvItem(b.language),
                checkCsvItem(b.rating.toString()),
                checkCsvItem(b.currentPage.toString()),
                checkCsvItem(b.notes),
                checkCsvItem(b.summary),
                checkCsvItem(prepareLabels(b.labels))
            ).joinToString(",")
        }
    }

    private fun prepareLabels(labels: List<BookLabel>): String {
        return labels.joinToString(";") { label ->
            "${label.title}:${label.hexColor}"
        }
    }

    private fun checkCsvItem(str: String?): String? {
        return if (str?.contains(",") == true) {
            "\"$str\""
        } else {
            str
        }
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        return externalStorageInteractor
            .listFilesInDirectory(
                BASE_DIR_NAME,
                filterPredicate = { fileName ->
                    fileName.endsWith(CSV_SUFFIX)
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

            val fileName = backupFile.name
            val data = fileName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val storageProvider = BackupStorageProvider.byAcronym(data[1])
            val timestamp = data[2].toLong()
            val books = Integer.parseInt(data[3])
            val device = fileName.substring(fileName.indexOf(data[4]), fileName.lastIndexOf("."))

            val metadata = BackupMetadata.WithLocalFile(
                id = fileName,
                fileName = fileName,
                device = device,
                storageProvider = storageProvider,
                books = books,
                timestamp = timestamp,
                localFilePath = backupFile,
                mimeType = CSV_MIME_TYPE
            )

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

    override fun mapBackupToBackupContent(entry: BackupMetadata): Single<BackupContent> {
        return singleOf {
                externalStorageInteractor.readFileContent(BASE_DIR_NAME, entry.fileName)
            }
            .flatMap(csvImporter::importFromContent)
            .map { books ->
                // Page records are not supported by this backup provider
                BackupContent(books, listOf())
            }
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

    private fun createFileName(timestamp: Long, books: Int): String {
        return "dante-backup_" +
            backupStorageProvider.acronym + "_" +
            timestamp + "_" +
            books + "_" +
            Build.MODEL +
            CSV_SUFFIX
    }

    companion object {

        private const val CSV_MIME_TYPE = "text/csv"
        private const val CSV_SUFFIX = ".csv"
        private const val BASE_DIR_NAME = "Dante"
        private const val RC_READ_WRITE_EXT_STORAGE = 0x5321

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
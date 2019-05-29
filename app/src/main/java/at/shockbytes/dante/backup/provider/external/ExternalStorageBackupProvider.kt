package at.shockbytes.dante.backup.provider.external

import android.Manifest
import android.os.Build
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupRepository
import at.shockbytes.dante.backup.model.BackupItem
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.provider.BackupProvider
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import timber.log.Timber
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    28.05.2019
 */
class ExternalStorageBackupProvider(
    private val schedulers: SchedulerFacade,
    private val gson: Gson
) : BackupProvider {

    override val backupStorageProvider = BackupStorageProvider.EXTERNAL_STORAGE

    override var isEnabled: Boolean = true

    private var baseFile: File? = null

    override fun initialize(activity: FragmentActivity?): Completable {
        return Completable.fromAction {

            if (activity == null) {
                throw BackupServiceConnectionException("${this.javaClass.simpleName} requires an activity!")
            }

            checkPermissions(activity)

            // If not enabled --> do nothing, we don't have the right permissions
            if (isEnabled) {
                baseFile = File(Environment.getExternalStorageDirectory(), "Dante")

                if (baseFile?.mkdirs() == false) {
                    isEnabled = false
                    throw IllegalStateException("Cannot create baseFile in $baseFile")
                }
            }
        }
    }

    private fun checkPermissions(activity: FragmentActivity) {

        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val hasPermissions = EasyPermissions.hasPermissions(activity, *permissions)

        // BackupProvider is enabled if it has permissions to read and write external storage
        isEnabled = hasPermissions

        if (!hasPermissions) {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(activity, RC_READ_WRITE_EXT_STORAGE, *permissions)
                    .setRationale(R.string.external_storage_rationale)
                    .setPositiveButtonText(R.string.rationale_ask_ok)
                    .setNegativeButtonText(R.string.rationale_ask_cancel)
                    .build()
            )
        }
    }

    override fun backup(books: List<BookEntity>): Completable {
        return Completable
            .fromCallable {

                val timestamp = System.currentTimeMillis()
                val fileName = createFileName(timestamp)
                val metadata = getMetadata(books.size, fileName, timestamp)

                val file = File(baseFile, fileName)

                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        throw IllegalStateException("Cannot create new file at location ${file.absolutePath}")
                    }
                }

                val content = gson.toJson(BackupItem(metadata, books))
                file.writeText(content)
            }
            .subscribeOn(schedulers.io)
    }

    private fun getMetadata(books: Int, fileName: String, timestamp: Long): BackupMetadata {
        return BackupMetadata(
            id = "",
            fileName = fileName,
            timestamp = timestamp,
            books = books,
            storageProvider = backupStorageProvider,
            device = Build.MODEL

        )
    }

    private fun createFileName(timestamp: Long): String {
        return "$timestamp${BackupRepository.BACKUP_ITEM_SUFFIX}"
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        return Single.fromCallable {

            baseFile
                ?.list { _, name ->
                    name.endsWith(BackupRepository.BACKUP_ITEM_SUFFIX)
                }
                ?.mapNotNull { backupFile ->
                    backupFileToBackupEntry(backupFile)
                }
        }
    }

    private fun backupFileToBackupEntry(backupFile: String): BackupMetadataState? {

        return try {

            val file = File(baseFile, backupFile)
            val content = file.readLines().joinToString(System.lineSeparator())
            val metadata = gson.fromJson(content, BackupItem::class.java).backupMetadata

            // Can only be active, there is no cached state
            BackupMetadataState.Active(metadata)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun removeBackupEntry(entry: BackupMetadata): Completable {
        return Completable.fromAction {

            val file = File(baseFile, entry.fileName)
            if (file.exists()) {
                if (!file.delete()) {
                    throw IllegalStateException("File ${entry.fileName} cannot be deleted!")
                }
            } else {
                throw NullPointerException("File associated to ${entry.fileName} does not exist!")
            }
        }
    }

    override fun removeAllBackupEntries(): Completable {
        return Completable.fromAction {
            baseFile
                ?.listFiles()
                ?.forEach { f ->
                    f.delete()
                }
        }
    }

    override fun mapEntryToBooks(entry: BackupMetadata): Single<List<BookEntity>> {
        return Single.fromCallable {
            val file = File(baseFile, entry.fileName)
            val content = file.readLines().joinToString(System.lineSeparator())
            gson.fromJson(content, BackupItem::class.java).books
        }
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }

    companion object {

        private const val RC_READ_WRITE_EXT_STORAGE = 0x5321
    }
}
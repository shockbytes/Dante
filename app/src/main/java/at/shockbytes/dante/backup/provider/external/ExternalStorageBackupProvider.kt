package at.shockbytes.dante.backup.provider.external

import android.Manifest
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupEntryState
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
import java.io.File
import java.io.FilenameFilter

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
        return Completable.fromCallable {

            val fileName = createFileName()
            val file = File(baseFile, fileName)

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw IllegalStateException("Cannot create new file at location ${file.absolutePath}")
                }
            }

            val content = "" // TODO what to write here
            file.writeText(content)
        }
    }

    private fun createFileName(): String {
        TODO("How should the filename look like")
    }

    override fun getBackupEntries(): Single<List<BackupEntryState>> {
        return Single.fromCallable {

            baseFile
                ?.list({ _, name ->
                    true // TODO
                })
                ?.map { backupFile ->
                    backupFileToBackupEntry(backupFile)
                }
        }
    }

    private fun backupFileToBackupEntry(backupFile: String): BackupEntryState {

        TODO()
        // return BackupEntryState.Active()
    }

    override fun removeBackupEntry(entry: BackupEntry): Completable {
        return Completable.fromAction {

            val file = File(baseFile, entry.fileName)
            if (file.exists()) {
                if (!file.delete()){
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

    override fun mapEntryToBooks(entry: BackupEntry): Single<List<BookEntity>> {
        return Single.fromCallable {

            val file = File(baseFile, entry.fileName)

            // TODO This is important!

            listOf<BookEntity>()
        }
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }

    companion object {

        private const val RC_READ_WRITE_EXT_STORAGE = 0x5321
    }
}
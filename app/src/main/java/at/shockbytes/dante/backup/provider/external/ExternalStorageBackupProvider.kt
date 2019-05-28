package at.shockbytes.dante.backup.provider.external

import android.Manifest
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

    override fun initialize(activity: FragmentActivity?): Completable {
        return Completable.fromAction {

            if (activity == null) {
                throw BackupServiceConnectionException("${this.javaClass.simpleName} requires an activity!")
            }

            checkPermissions(activity)
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
        // TODO Implement this method...
        return Completable.complete()
    }

    override fun getBackupEntries(): Single<List<BackupEntryState>> {
        // TODO Implement this method...
        return Single.just(
            listOf(
                BackupEntryState.Active(
                    BackupEntry(
                        id = "id1",
                        fileName = "filename1",
                        device = "Nexus 4",
                        storageProvider = backupStorageProvider,
                        books = 64,
                        timestamp = System.currentTimeMillis()
                    )
                ),
                BackupEntryState.Inactive(
                    BackupEntry(
                        id = "id2",
                        fileName = "filename2",
                        device = "Nexus 7",
                        storageProvider = backupStorageProvider,
                        books = 128,
                        timestamp = System.currentTimeMillis() - 100000000000L
                    )
                )
            )
        )
    }

    override fun removeBackupEntry(entry: BackupEntry): Completable {
        // TODO Implement this method...
        return Completable.complete()
    }

    override fun removeAllBackupEntries(): Completable {
        // TODO Implement this method...
        return Completable.complete()
    }

    override fun mapEntryToBooks(entry: BackupEntry): Single<List<BookEntity>> {
        // TODO Implement this method...
        return Single.just(listOf())
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }

    companion object {

        private const val RC_READ_WRITE_EXT_STORAGE = 0x5321
    }
}
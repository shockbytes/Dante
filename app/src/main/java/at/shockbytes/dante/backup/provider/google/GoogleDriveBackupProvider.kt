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
import at.shockbytes.dante.signin.GoogleFirebaseSignInManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.MetadataBuffer
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Deprecated(
    message = "Deprecated since the Google Drive API got deprecated",
    replaceWith = ReplaceWith("GoogleDriveRestBackupProvider")
)
class GoogleDriveBackupProvider(
    private val signInManager: GoogleFirebaseSignInManager,
    private val schedulers: SchedulerFacade,
    private val gson: Gson
) : BackupProvider {

    private lateinit var client: DriveResourceClient

    override var isEnabled: Boolean = false

    override val backupStorageProvider = BackupStorageProvider.GOOGLE_DRIVE

    override fun mapEntryToBooks(entry: BackupMetadata): Single<List<BookEntity>> {
        return Single
            .fromCallable {

                val file = DriveId.decodeFromString(entry.id).asDriveFile()
                val result = client.openFile(file, DriveFile.MODE_READ_ONLY)

                val contents = Tasks.await(result)
                val reader = BufferedReader(InputStreamReader(contents?.inputStream))
                val builder = StringBuilder()

                for (line in reader.lineSequence()) {
                    builder.append(line)
                }
                val contentsAsString = builder.toString()
                client.discardContents(contents) // Close contents

                val list: List<BookEntity> = gson.fromJson(contentsAsString,
                    object : TypeToken<List<BookEntity>>() {}.type)
                list
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    override fun backup(books: List<BookEntity>): Completable {

        if (books.isEmpty()) {
            return Completable.error(BackupException("No books to backup"))
        }

        val content = gson.toJson(books)
        val filename = createFilename(books.size)

        return createFile(filename, content)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    override fun initialize(activity: FragmentActivity?): Completable {

        if (activity == null) {
            isEnabled = false
            throw BackupServiceConnectionException("This backup provider requires an activity!")
        }

        return Completable.fromAction {

            val initializedClient = signInManager.getGoogleAccount()?.let { account ->
                Drive.getDriveResourceClient(activity, account)
            }

            if (initializedClient != null) {
                isEnabled = true
                client = initializedClient
            } else {
                isEnabled = false
            }
        }
    }

    override fun teardown(): Completable {
        return Completable.complete()
    }

    override fun getBackupEntries(): Single<List<BackupMetadataState>> {
        return Single
            .fromCallable {
                client.appFolder?.let { folder ->
                    val appFolder = Tasks.await(folder)
                    fromMetadataToBackupEntries(Tasks.await(client.listChildren(appFolder)))
                } ?: listOf()
            }
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
        return Completable
            .fromAction {
                if (!deleteDriveFile(DriveId.decodeFromString(entry.id))) {
                    Completable.error(Throwable(BackupException("Cannot delete backup entry: " + entry.fileName)))
                }
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    override fun removeAllBackupEntries(): Completable {
        return Completable
            .fromAction {
                val appFolder = Tasks.await(client.appFolder)
                Tasks.await(client.listChildren(appFolder))
                    .all { deleteDriveFile(it.driveId) }
                    .let { allDeleted ->
                        if (!allDeleted) {
                            throw BackupException("Cannot remove all backup entries!")
                        }
                    }
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    // -----------------------------------------------------------------------------

    private fun deleteDriveFile(driveId: DriveId): Boolean {
        return client.delete(driveId.asDriveResource())?.isSuccessful ?: false
    }

    private fun fromMetadataToBackupEntries(result: MetadataBuffer?): List<BackupMetadata> {

        val entries = result?.mapNotNullTo(mutableListOf()) { buffer ->

            val fileId = buffer.driveId.encodeToString()
            val fileName = buffer.title
            try {

                Timber.i("File name of backup file: $fileName")
                val data = fileName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val storageProviderAcronym = data[0]
                val storageProvider = BackupStorageProvider.byAcronym(storageProviderAcronym)
                val device = data[4].substring(0, data[4].lastIndexOf("."))
                val timestamp = java.lang.Long.parseLong(data[2])
                val books = Integer.parseInt(data[3])

                BackupMetadata(
                    id = fileId,
                    fileName = fileName,
                    device = device,
                    storageProvider = storageProvider,
                    books = books,
                    timestamp = timestamp
                )
            } catch (e: Exception) {
                Timber.e(e, "Cannot parse file: $fileName")
                null
            }
        }

        result?.release()
        return entries ?: listOf()
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

    private fun createFile(filename: String, content: String): Completable {
        return Completable.fromAction {
            val changeSet = MetadataChangeSet.Builder()
                .setTitle(filename)
                .setMimeType("application/json")
                .build()

            val driveContents = Tasks.await(client.createContents())

            val out = driveContents?.outputStream
            val writer = BufferedWriter(OutputStreamWriter(out))

            try {
                writer.write(content)
            } catch (e: IOException) {
                e.printStackTrace()
                throw e
            } finally {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            val driveFileResult = Tasks.await(client.appFolder)
            Tasks.await(client.createFile(driveFileResult, changeSet, driveContents))
                ?: throw NullPointerException("Result of backup creation is null!")
        }
    }
}
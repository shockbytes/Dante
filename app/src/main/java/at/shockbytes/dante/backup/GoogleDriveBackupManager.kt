package at.shockbytes.dante.backup

import android.content.SharedPreferences
import android.os.Build
import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupEntry
import at.shockbytes.dante.backup.model.BackupException
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.model.RestoreStrategy
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.signin.GoogleSignInManager
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

/**
 * Author:  Martin Macheiner
 * Date:    30.04.2017
 */
class GoogleDriveBackupManager(
    private val preferences: SharedPreferences,
    private val signInManager: GoogleSignInManager,
    private val schedulers: SchedulerFacade,
    private val gson: Gson
) : BackupManager {

    private lateinit var client: DriveResourceClient

    override var lastBackupTime: Long
        get() = preferences.getLong(LAST_BACKUP, 0)
        set(value) = preferences.edit().putLong(LAST_BACKUP, value).apply()

    override val backupList: Single<List<BackupEntry>>
        get() =
            Single
                .fromCallable {
                    client.appFolder?.let { folder ->
                        val appFolder = Tasks.await(folder)
                        fromMetadataToBackupEntries(Tasks.await(client.listChildren(appFolder)))
                    } ?: listOf()
                }
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.ui)

    override fun connect(activity: FragmentActivity): Completable {
        return Completable.fromAction {
            signInManager.getGoogleAccount()?.let { account ->
                client = Drive.getDriveResourceClient(activity, account)
            }
                ?: throw BackupServiceConnectionException("Cannot access Google Account. Account = null")
        }
    }

    override fun close(books: List<BookEntity>?) = Unit

    override fun removeBackupEntry(entry: BackupEntry): Completable {
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

    override fun backup(books: List<BookEntity>): Completable {

        if (books.isEmpty()) {
            throw BackupException("No books to backup")
        }

        val content = gson.toJson(books)
        val filename = createFilename(books.size)

        return createFile(filename, content)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }

    override fun restoreBackup(
        entry: BackupEntry,
        bookDao: BookEntityDao,
        strategy: RestoreStrategy
    ): Completable {
        return booksFromEntry(entry)
            .flatMapCompletable { books ->
                bookDao.restoreBackup(books, strategy)
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
    }
    // -----------------------------------------------------------------------------

    private fun deleteDriveFile(driveId: DriveId): Boolean {
        return client.delete(driveId.asDriveResource())?.isSuccessful ?: false
    }

    private fun fromMetadataToBackupEntries(result: MetadataBuffer?): List<BackupEntry> {

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

                BackupEntry(
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

        return STORAGE_TYPE + "_" +
            type + "_" +
            timestamp + "_" +
            books + "_" +
            Build.MODEL + ".json"
    }

    private fun booksFromEntry(entry: BackupEntry): Single<List<BookEntity>> {
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

    private fun createFile(filename: String, content: String): Completable {
        return Completable.fromAction {
            val changeSet = MetadataChangeSet.Builder()
                .setTitle(filename)
                .setMimeType(MIME_JSON)
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
            val createResult = Tasks.await(client.createFile(driveFileResult, changeSet, driveContents))

            if (createResult != null) {
                lastBackupTime = System.currentTimeMillis()
            } else {
                throw NullPointerException("Result of backup creation is null!")
            }
        }
    }

    companion object {

        private const val LAST_BACKUP = "google_drive_last_backup"
        private const val STORAGE_TYPE = "gdrive"
        private const val MIME_JSON = "application/json"
    }
}
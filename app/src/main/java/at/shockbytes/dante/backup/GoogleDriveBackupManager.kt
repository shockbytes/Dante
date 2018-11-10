package at.shockbytes.dante.backup

import android.content.SharedPreferences
import android.os.Build
import android.support.v4.app.FragmentActivity
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.signin.GoogleSignInManager
import com.google.android.gms.drive.*
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.*
import java.util.*
import javax.inject.Named

/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */

class GoogleDriveBackupManager(private val preferences: SharedPreferences,
                               private val signInManager: GoogleSignInManager,
                               @param:Named("backup_gson") private val gson: Gson) : BackupManager {

    override val lastBackupTime: Long
        get() = preferences.getLong(LAST_BACKUP, 0)

    override val backupList: Single<List<BackupEntry>>
        get() {
            return Single.fromCallable {
                client?.let { client ->
                    client.appFolder?.let { folder ->
                        val appFolder = Tasks.await(folder)
                        return@fromCallable fromMetadataToBackupEntries(Tasks.await(client.listChildren(appFolder)))
                    }
                }
                listOf<BackupEntry>()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }

    private var client: DriveResourceClient? = null

    override fun connect(activity: FragmentActivity) {

        signInManager.getGoogleAccount()?.let { account ->
            client = Drive.getDriveResourceClient(activity, account)
        }
    }

    override fun close(books: List<BookEntity>?) {
    }

    override fun removeBackupEntry(entry: BackupEntry): Completable {
        return Completable.fromAction {
            if (!deleteDriveFile(DriveId.decodeFromString(entry.fileId))) {
                Completable.error(Throwable(
                        BackupException("Cannot delete backup entry: " + entry.fileName)))
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun removeAllBackupEntries(): Completable {
        return Completable.fromAction {

            val appFolder = Tasks.await(client?.appFolder!!)
            Tasks.await(client?.listChildren(appFolder)!!)
                    .filterNot { deleteDriveFile(it.driveId) }
                    .forEach { Completable.error(Throwable(BackupException("Can't delete backups"))) }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun backup(booksObservable: Flowable<List<BookEntity>>): Completable {
        return Completable.fromAction {

            val books = booksObservable.blockingFirst()
            if (books.isNotEmpty()) {
                val content = gson.toJson(books)
                val filename = createFilename(books.size)
                createFile(filename, content)
            } else {
                Completable.error(BackupException("No books to backup"))
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun restoreBackup(entry: BackupEntry, bookDao: BookEntityDao,
                               strategy: BackupManager.RestoreStrategy): Completable {

        return if (client != null) {
            Completable.fromAction {
                booksFromEntry(entry)
                        .subscribe({ books ->
                            bookDao.restoreBackup(books, strategy)
                        }, { throwable -> Timber.e(throwable) })
            }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread())
        } else {
            Completable.error(NullPointerException("DriveClient is null!"))
        }
    }

// -----------------------------------------------------------------------------

    private fun setLastBackupTime(millis: Long) {
        preferences.edit().putLong(LAST_BACKUP, millis).apply()
    }

    private fun deleteDriveFile(driveId: DriveId): Boolean {
        return client?.delete(driveId.asDriveResource())?.isSuccessful ?: false
    }

    private fun fromMetadataToBackupEntries(result: MetadataBuffer?): List<BackupEntry> {

        val entries = ArrayList<BackupEntry>()

        result?.forEach { buffer ->

            val fileId = buffer.driveId.encodeToString()
            val fileName = buffer.title
            try {

                val data = fileName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val storageProvider = data[0]
                val device = data[4].substring(0, data[4].lastIndexOf("."))
                val isAutoBackup = data[1] == "auto"
                val timestamp = java.lang.Long.parseLong(data[2])
                val books = Integer.parseInt(data[3])

                entries.add(BackupEntry(fileId, fileName, device, storageProvider,
                        books, timestamp, isAutoBackup))

            } catch (e: Exception) {
                Timber.e(e, "Cannot parse file: $fileName")
            }
        }

        result?.release()
        return entries
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
        return Single.fromCallable {
            val file = DriveId.decodeFromString(entry.fileId).asDriveFile()
            val result = client?.openFile(file, DriveFile.MODE_READ_ONLY)

            val contents = Tasks.await(result!!)
            val reader = BufferedReader(InputStreamReader(contents?.inputStream))
            val builder = StringBuilder()

            for (line in reader.lineSequence()) {
                builder.append(line)
            }
            val contentsAsString = builder.toString()
            client?.discardContents(contents) // Close contents

            val list: List<BookEntity> = gson.fromJson(contentsAsString,
                    object : TypeToken<List<BookEntity>>() {}.type)
            list

        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    private fun createFile(filename: String, content: String): Completable {

        val changeSet = MetadataChangeSet.Builder()
                .setTitle(filename)
                .setMimeType(MIME_JSON)
                .build()

        val driveContents = Tasks.await(client?.createContents()!!)

        val out = driveContents?.outputStream
        val writer = BufferedWriter(OutputStreamWriter(out))

        try {
            writer.write(content)
        } catch (e: IOException) {
            e.printStackTrace()
            return Completable.error(Throwable(e.localizedMessage))
        } finally {
            try {
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val driveFileResult = Tasks.await(client?.appFolder!!)
        val createResult = Tasks.await(client?.createFile(driveFileResult, changeSet, driveContents)!!)

        return if (createResult != null) {
            setLastBackupTime(System.currentTimeMillis())
            Completable.complete()
        } else {
            Completable.error(NullPointerException())
        }
    }

    companion object {

        private const val LAST_BACKUP = "google_drive_last_backup"
        private const val STORAGE_TYPE = "gdrive"
        private const val MIME_JSON = "application/json"
    }

}

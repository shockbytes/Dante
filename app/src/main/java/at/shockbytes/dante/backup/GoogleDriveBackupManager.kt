package at.shockbytes.dante.backup

import android.content.SharedPreferences
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.util.Log
import at.shockbytes.dante.signin.GoogleSignInManager
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.books.BookManager
import com.google.android.gms.drive.*
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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


    override var isAutoBackupEnabled: Boolean
        get() = preferences.getBoolean(AUTO_BACKUP_ENABLED, false)
        set(value) {
            preferences.edit().putBoolean(AUTO_BACKUP_ENABLED, value).apply()
        }

    override val lastBackupTime: Long
        get() = preferences.getLong(LAST_BACKUP, 0)

    override val backupList: Observable<List<BackupEntry>>
        get() {
            return Observable.defer {

                val appFolder = Tasks.await(client?.appFolder!!)
                val entries = fromMetadataToBackupEntries(Tasks.await(client?.listChildren(appFolder)!!))
                return@defer Observable.just(entries)

            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }

    private var activity: FragmentActivity? = null

    private var client: DriveResourceClient? = null

    override fun connect(activity: FragmentActivity) {
        this.activity = activity

        val account = signInManager.getAccount(activity)
        if (account != null) {
            client = Drive.getDriveResourceClient(activity, account)
        }
    }

    override fun close(books: List<Book>?) {

        if (isAutoBackupEnabled && books != null) {
            backup(books) // TODO Must subscribe to backup the books
        }
        activity = null
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

    override fun backup(books: List<Book>): Completable {

        if (books.isEmpty()) {
            return Completable.error(BackupException("No books to backup"))
        }

        // Must be outside the observable, because otherwise this will cause a RealmException
        val content = gson.toJson(books)
        val filename = createFilename(books.size)

        return Completable.fromAction {

            // Either auto backup is disabled, or auto backup enabled and last backup
            // was longer than two weeks ago
            val backupThreshold = System.currentTimeMillis() - MILLIS_TWO_WEEKS
            if (!isAutoBackupEnabled || lastBackupTime < backupThreshold) {
                createFile(filename, content)
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun restoreBackup(entry: BackupEntry, bookManager: BookManager,
                               strategy: BackupManager.RestoreStrategy): Completable {

        return if (client != null) {
            Completable.fromAction {
                booksFromEntry(entry).subscribe { books ->
                    bookManager.restoreBackup(books, strategy)
                }
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

        if (result != null) {
            for (buffer in result) {

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
                    e.printStackTrace()
                    Log.wtf("Dante", "Cannot parse file: " + fileName)
                }

            }
            result.release()
        }
        return entries
    }

    private fun createFilename(books: Int): String {

        val timestamp = System.currentTimeMillis()
        val type = if (isAutoBackupEnabled) "auto" else "man"

        return STORAGE_TYPE + "_" +
                type + "_" +
                timestamp + "_" +
                books + "_" +
                Build.MODEL + ".json"
    }

    private fun booksFromEntry(entry: BackupEntry): Observable<List<Book>> {
        return Observable.defer {
            val file = DriveId.decodeFromString(entry.fileId).asDriveFile()
            val result = client?.openFile(file, DriveFile.MODE_READ_ONLY)

            val contents = Tasks.await(result!!)
            val reader = BufferedReader(InputStreamReader(contents?.inputStream))
            val builder = StringBuilder()

            try {

                for (line in reader.lineSequence()) {
                    builder.append(line)
                }
                val contentsAsString = builder.toString()
                client?.discardContents(contents) // Close contents

                val list: List<Book> = gson.fromJson(contentsAsString,
                        object : TypeToken<List<Book>>() {}.type)
                Observable.just(list)

            } catch (e: IOException) {
                e.printStackTrace()
                Observable.just(listOf<Book>()) // Return empty array instead of a null array
            }
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

        private val LAST_BACKUP = "google_drive_last_backup"
        private val AUTO_BACKUP_ENABLED = "google_drive_auto_backup_enabled"
        private val STORAGE_TYPE = "gdrive"
        private val MIME_JSON = "application/json"

        private val MILLIS_TWO_WEEKS = 1209600000L
    }

}

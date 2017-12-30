package at.shockbytes.dante.util.backup

import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.books.BookManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */

class GoogleDriveBackupManager @Inject
constructor(private val context: Context, private val preferences: SharedPreferences,
            @param:Named("backup_gson") private val gson: Gson) : BackupManager,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var activity: FragmentActivity? = null
    private var onStatusListener: BackupManager.OnConnectionStatusListener? = null

    private var client: DriveResourceClient? = null


    override fun connect(activity: FragmentActivity,
                         onStatusListener: BackupManager.OnConnectionStatusListener) {
        this.activity = activity
        this.onStatusListener = onStatusListener

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val signInClient = GoogleSignIn.getClient(activity, signInOptions)
        signInClient.silentSignIn().addOnCompleteListener {
            client = Drive.getDriveResourceClient(activity, it.result)
        }

    }

    override fun disconnect() {
        activity = null
    }

    override fun isAutoBackupEnabled(): Boolean {
        return preferences.getBoolean(AUTO_BACKUP_ENABLED, false)
    }

    override fun setAutoBackupEnabled(autoBackupEnabled: Boolean) {
        preferences.edit().putBoolean(AUTO_BACKUP_ENABLED, autoBackupEnabled).apply()
    }

    override fun getLastBackupTime(): Long {
        return preferences.getLong(LAST_BACKUP, 0)
    }

    override fun getBackupList(): Observable<List<BackupEntry>> {

        return Observable.defer {
            client?.appFolder?.result?.let {
                val entries = fromMetadataToBackupEntries(client?.listChildren(it)?.result)
                client?.listChildren(it)?.result?.release()
                return@defer Observable.just(entries)
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun removeBackupEntry(entry: BackupEntry): Completable {
        return Completable.create {
            if (!deleteDriveFile(DriveId.decodeFromString(entry.fileId))) {
                Completable.error(Throwable(
                        BackupException("Cannot delete backup entry: " + entry.fileName)))
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun removeAllBackupEntries(): Completable {
        return Completable.create {
            client?.appFolder?.result?.let {
                val res = client?.listChildren(it)?.result
                res?.filterNot {
                    // Throw an exception if even 1 file cannot be deleted
                    deleteDriveFile(it.driveId)
                }?.forEach {
                    Completable.error(Throwable(BackupException("Can't delete one of the backups")))
                }
            }

        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun backup(books: List<Book>): Completable {

        if (books.isEmpty()) {
            return Completable.error(BackupException("No books to backup"))
        }

        // Must be outside the observable, because otherwise this will cause a RealmException
        val content = gson.toJson(books)
        val filename = createFilename(books.size)

        return Completable.create {

            // Either auto backup is disabled, or auto backup enabled and last backup
            // was longer than two weeks ago
            val backupThreshold = System.currentTimeMillis() - MILLIS_TWO_WEEKS
            if (!isAutoBackupEnabled || lastBackupTime < backupThreshold) {
                createFile(filename, content)
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun restoreBackup(activity: FragmentActivity, entry: BackupEntry,
                               bookManager: BookManager, strategy: BackupManager.RestoreStrategy): Completable {

        return Completable.create {
            Log.wtf("Dante", "restore backup")
            val books = booksFromEntry(entry)
            bookManager.restoreBackup(activity, books, strategy)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun onConnected(bundle: Bundle?) {
        onStatusListener?.onConnected()
    }

    override fun onConnectionSuspended(i: Int) {}

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

        if (connectionResult.hasResolution()) {

            try {
                connectionResult.startResolutionForResult(activity, BackupManager.RESOLVE_CONNECTION_REQUEST_CODE)
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }

        } else {
            onStatusListener?.onConnectionFailed()
            GoogleApiAvailability.getInstance().getErrorDialog(activity, connectionResult.errorCode,
                    PLAY_SERVICES_RESOLUTION_REQUEST).show()
        }
    }

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

    private fun booksFromEntry(entry: BackupEntry): List<Book> {

        Log.wtf("Dante", "books from Entry")
        val file = DriveId.decodeFromString(entry.fileId).asDriveFile()
        val result = client?.openFile(file, DriveFile.MODE_READ_ONLY)

        val contents = result?.result
        val reader = BufferedReader(InputStreamReader(contents?.inputStream))
        val builder = StringBuilder()

        try {

            for (line in reader.lineSequence()) {
                builder.append(line)
            }

            val contentsAsString = builder.toString()
            // Close contents
            if (result?.result != null) {
                client?.discardContents(result.result)
            }
            return gson.fromJson(contentsAsString, object : TypeToken<ArrayList<Book>>() {}.type)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Return empty array instead of a null array
        return ArrayList()
    }

    private fun createFile(filename: String, content: String): Completable {

        val changeSet = MetadataChangeSet.Builder()
                .setTitle(filename)
                .setMimeType(MIME_JSON)
                .build()

        val driveContents = client?.createContents()?.result

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

        val driveFileResult = client?.appFolder?.result?.let {
            client?.createFile(it, changeSet, driveContents)
        }

        return if (driveFileResult?.isSuccessful == true) {
            setLastBackupTime(System.currentTimeMillis())
            Completable.complete()
        } else {
            Completable.error(Throwable(driveFileResult?.exception))
        }
    }

    companion object {

        private val LAST_BACKUP = "google_drive_last_backup"
        private val AUTO_BACKUP_ENABLED = "google_drive_auto_backup_enabled"
        private val STORAGE_TYPE = "gdrive"
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 0x8764
        private val MIME_JSON = "application/json"

        private val MILLIS_TWO_WEEKS = 1209600000L
    }

}

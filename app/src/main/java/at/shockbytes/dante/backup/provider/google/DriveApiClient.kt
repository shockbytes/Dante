package at.shockbytes.dante.backup.provider.google

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupException
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.signin.GoogleFirebaseSignInManager
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.singleOf
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.MetadataBuffer
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.tasks.Tasks
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Deprecated("Use DriveRestClient instead")
class DriveApiClient(private val signInManager: GoogleFirebaseSignInManager) : DriveClient {

    private lateinit var client: DriveResourceClient

    override fun initialize(activity: FragmentActivity): Completable {
        return completableOf {
            client = signInManager.getGoogleAccount()!!.let { account ->
                Drive.getDriveResourceClient(activity, account)
            }
        }
    }

    override fun readFileAsString(fileId: String): Single<String> {
        return singleOf {
            val file = DriveId.decodeFromString(fileId).asDriveFile()
            val result = client.openFile(file, DriveFile.MODE_READ_ONLY)

            val contents = Tasks.await(result)
            val reader = BufferedReader(InputStreamReader(contents?.inputStream))
            val contentBuilder = StringBuilder()

            for (line in reader.lineSequence()) {
                contentBuilder.append(line)
            }

            client.discardContents(contents) // Close contents

            contentBuilder.toString()
        }
    }

    override fun createFile(filename: String, content: String): Completable {
        return completableOf {
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

    override fun deleteFile(fileId: String, fileName: String): Completable {
        return completableOf {
            if (!deleteDriveFile(DriveId.decodeFromString(fileId))) {
                val exception = BackupException("Cannot delete backup entry: $fileName", fileName)
                Completable.error(exception)
            }
        }
    }

    override fun deleteListedFiles(): Completable {
        return completableOf {
            val appFolder = Tasks.await(client.appFolder)
            Tasks.await(client.listChildren(appFolder))
                .all { deleteDriveFile(it.driveId) }
                .let { allDeleted ->
                    if (!allDeleted) {
                        throw BackupException("Cannot remove all backup entries!")
                    }
                }
        }
    }

    private fun deleteDriveFile(driveId: DriveId): Boolean {
        return client.delete(driveId.asDriveResource())?.isSuccessful ?: false
    }

    override fun listBackupFiles(): Single<List<BackupMetadata>> {
        return singleOf {
            client.appFolder?.let { folder ->
                val appFolder = Tasks.await(folder)
                fromMetadataToBackupEntries(Tasks.await(client.listChildren(appFolder)))
            } ?: listOf()
        }
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
}
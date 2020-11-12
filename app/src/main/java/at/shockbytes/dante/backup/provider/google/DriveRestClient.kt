package at.shockbytes.dante.backup.provider.google

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.signin.GoogleFirebaseSignInManager
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.merge
import at.shockbytes.dante.util.singleOf
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Collections

class DriveRestClient(
    private val signInManager: GoogleFirebaseSignInManager,
    private val mimeType: String = "application/json",
    private val parentFolder: String = "appDataFolder", // TODO = AppFolder? root/appDataFolder
    private val spaces: String = "appDataFolder" // TODO Verify?
) : DriveClient {

    private lateinit var drive: Drive

    override fun initialize(activity: FragmentActivity): Completable {
        return completableOf {
            // Use the authenticated account to sign in to the Drive service.
            val credential: GoogleAccountCredential = GoogleAccountCredential
                .usingOAuth2(activity, Collections.singleton(DriveScopes.DRIVE_FILE))
                .apply {
                    selectedAccount = signInManager.getGoogleAccount()!!.account // Fail here if null
                }
            drive = Drive
                .Builder(
                    NetHttpTransport(),
                    GsonFactory(),
                    credential
                )
                .setApplicationName(APP_NAME)
                .build()
        }
    }

    override fun readFileAsString(fileId: String): Single<String> {
        return singleOf {
            // val metadata: File = drive.files().get(fileId).execute()
            // val name: String = metadata.name
            drive.files().get(fileId).executeMediaAsInputStream().use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            }
        }
    }

    override fun createFile(filename: String, content: String): Completable {
        return completableOf {

            // 1. Create file
            val metadata: File = File()
                .setParents(Collections.singletonList(parentFolder))
                .setMimeType(mimeType)
                .setName(filename)
            val createdFile: File = drive.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")

            // 2. Write to File
            val contentStream = ByteArrayContent.fromString(mimeType, content)
            drive.files().update(createdFile.id, metadata, contentStream).execute()
        }
    }

    override fun deleteFile(fileId: String, fileName: String): Completable {
        return completableOf {
            drive.files().delete(fileId).execute()
        }
    }

    override fun deleteListedFiles(): Completable {
        return listBackupFiles()
            .flatMapCompletable { backupMetadataSet ->
                backupMetadataSet
                    .map { file ->
                        deleteFile(file.id, file.fileName)
                    }
                    .merge()
            }
    }

    override fun listBackupFiles(): Single<List<BackupMetadata>> {
        return singleOf {
            drive.files().list().setSpaces(spaces).execute().files.map { file ->
                Timber.e(file.toPrettyString())

                val fileName = file.name
                val data = fileName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val storageProviderAcronym = data[0]
                val storageProvider = BackupStorageProvider.byAcronym(storageProviderAcronym)
                val device = data[4].substring(0, data[4].lastIndexOf("."))
                val timestamp = java.lang.Long.parseLong(data[2])
                val books = Integer.parseInt(data[3])

                BackupMetadata.Standard(
                    id = file.id,
                    fileName = fileName,
                    device = device,
                    storageProvider = storageProvider,
                    books = books,
                    timestamp = timestamp
                )
            }
        }
    }

    companion object {

        private const val APP_NAME = "Dante"
    }
}
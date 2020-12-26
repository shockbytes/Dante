package at.shockbytes.dante.backup.provider.google

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.core.login.UnauthenticatedUserException
import at.shockbytes.dante.core.login.GoogleFirebaseLoginRepository
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.merge
import com.google.android.gms.tasks.Tasks
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
import java.lang.Exception
import java.util.Collections
import java.util.concurrent.Executors

class DriveRestClient(
    private val signInManager: GoogleFirebaseLoginRepository
) : DriveClient {

    private lateinit var drive: Drive

    private val executor = Executors.newSingleThreadExecutor()

    override fun initialize(activity: FragmentActivity): Completable {
        return Completable.create { emitter ->

            val account = signInManager.getGoogleAccount()?.account

            if (account != null) {
                // Use the authenticated account to sign in to the Drive service.
                val credential: GoogleAccountCredential = GoogleAccountCredential
                    .usingOAuth2(activity, Collections.singleton(DriveScopes.DRIVE_FILE))
                    .apply {
                        selectedAccount = account
                    }

                drive = Drive
                    .Builder(
                        NetHttpTransport(),
                        GsonFactory(),
                        credential
                    )
                    .setApplicationName(APP_NAME)
                    .build()

                emitter.onComplete()
            } else {
                emitter.tryOnError(UnauthenticatedUserException())
            }
        }
    }

    override fun readFileAsString(fileId: String): Single<String> {
        return Single.create { emitter ->

            Tasks.call(executor) {
                val metadata: File = drive.files().get(fileId).execute()
                val name: String = metadata.name
                log("Read filename: $name")

                try {
                    drive.files().get(fileId).executeMediaAsInputStream().use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream))
                            .use { it.readText() }
                            .also(::log)
                            .let(emitter::onSuccess)
                    }
                } catch (exception: Exception) {
                    Timber.e(exception)
                    emitter.tryOnError(exception)
                }
            }
        }
    }

    override fun createFile(filename: String, content: String): Completable {
        return Completable.create { emitter ->
            Tasks.call(executor) {

                try {
                    val metadata: File = File()
                        .setParents(Collections.singletonList(PARENT_FOLDER))
                        .setMimeType(MIME_TYPE)
                        .setName(filename)

                    val contentStream = ByteArrayContent.fromString(MIME_TYPE, content)

                    log("Create file with content $content")

                    val file = drive.files().create(metadata, contentStream).execute()
                        ?: throw IOException("Null result when requesting file creation.")

                    log("File created with ID: <${file.id}>")

                    emitter.onComplete()
                } catch (e: Exception) {
                    emitter.tryOnError(e)
                }
            }
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
        return Single.create { emitter ->
            Tasks.call(executor) {

                try {
                    drive.files().list().setSpaces(SPACES)
                        .execute()
                        .files
                        .mapNotNull { file ->

                            val fileName = file.name
                            val data = fileName.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            val storageProvider = BackupStorageProvider.byAcronym(data[0])

                            if (isCorrectStorageProvider(storageProvider)) {

                                val device = fileName.substring(
                                    fileName.indexOf(data[4]),
                                    fileName.lastIndexOf(".")
                                )

                                val timestamp = data[2].toLong()
                                val books = data[3].toInt()

                                BackupMetadata.Standard(
                                    id = file.id,
                                    fileName = fileName,
                                    device = device,
                                    storageProvider = storageProvider,
                                    books = books,
                                    timestamp = timestamp
                                )
                            } else null
                        }
                        .let(emitter::onSuccess)
                } catch (e: Exception) {
                    emitter.tryOnError(e)
                }
            }
        }
    }

    /**
     * This call is necessary because there is a Google Drive legacy implementation, which may
     * reside next to the new GOOGLE_DRIVE format. For simplicity, the old format got fully
     * discarded since it stored the data obfuscated.
     */
    private fun isCorrectStorageProvider(storageProvider: BackupStorageProvider): Boolean {
        return storageProvider == BackupStorageProvider.GOOGLE_DRIVE
    }

    private fun log(msg: String) {
        Timber.d(msg)
    }

    companion object {

        private const val APP_NAME = "Dante"
        private const val MIME_TYPE = "application/json"
        private const val PARENT_FOLDER = "appDataFolder"
        private const val SPACES = "appDataFolder"
    }
}
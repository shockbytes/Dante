package at.shockbytes.dante.backup.provider.google

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.signin.GoogleFirebaseSignInManager
import io.reactivex.Completable
import io.reactivex.Single

class DriveRestClient(private val signInManager: GoogleFirebaseSignInManager) : DriveClient {

    override fun initialize(activity: FragmentActivity): Completable {
        TODO("Not yet implemented")
    }

    override fun readFileAsString(fileId: String): Single<String> {
        TODO("Not yet implemented")
    }

    override fun createFile(filename: String, content: String): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteFile(fileId: String, fileName: String): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteListedFiles(): Completable {
        TODO("Not yet implemented")
    }

    override fun listBackupFiles(): Single<List<BackupMetadata>> {
        TODO("Not yet implemented")
    }
}
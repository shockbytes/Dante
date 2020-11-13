package at.shockbytes.dante.backup.provider.google

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupMetadata
import io.reactivex.Completable
import io.reactivex.Single

interface DriveClient {

    fun initialize(activity: FragmentActivity): Completable

    fun readFileAsString(fileId: String): Single<String>

    fun createFile(filename: String, content: String): Completable

    fun deleteFile(fileId: String, fileName: String): Completable

    fun deleteListedFiles(): Completable

    fun listBackupFiles(): Single<List<BackupMetadata>>
}
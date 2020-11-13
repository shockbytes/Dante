package at.shockbytes.dante.backup.provider

import androidx.fragment.app.FragmentActivity
import at.shockbytes.dante.backup.model.BackupContent
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import io.reactivex.Completable
import io.reactivex.Single

interface BackupProvider {

    val backupStorageProvider: BackupStorageProvider

    var isEnabled: Boolean

    fun initialize(activity: FragmentActivity? = null): Completable

    fun backup(backupContent: BackupContent): Completable

    fun getBackupEntries(): Single<List<BackupMetadataState>>

    fun removeBackupEntry(entry: BackupMetadata): Completable

    fun removeAllBackupEntries(): Completable

    fun mapBackupToBackupContent(entry: BackupMetadata): Single<BackupContent>

    fun teardown(): Completable
}
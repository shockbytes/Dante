package at.shockbytes.dante.backup.provider.shockbytes.storage

import at.shockbytes.dante.backup.model.BackupMetadataState

interface InactiveShockbytesBackupStorage {

    fun storeInactiveItems(items: List<BackupMetadataState>)

    fun getInactiveItems(): List<BackupMetadataState>
}

package at.shockbytes.dante.backup.provider.shockbytes.storage

import at.shockbytes.dante.backup.model.BackupEntryState

interface InactiveShockbytesBackupStorage {

    fun storeInactiveItems(items: List<BackupEntryState>)

    fun getInactiveItems(): List<BackupEntryState>
}

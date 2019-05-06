package at.shockbytes.dante.backup.model

sealed class BackupEntryState {

    abstract val entry: BackupEntry

    data class Active(override val entry: BackupEntry): BackupEntryState()

    data class Inactive(override val entry: BackupEntry): BackupEntryState()

}
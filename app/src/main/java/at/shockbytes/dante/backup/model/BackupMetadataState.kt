package at.shockbytes.dante.backup.model

sealed class BackupMetadataState {

    abstract val entry: BackupMetadata

    val timestamp: Long
        get() = entry.timestamp

    data class Active(override val entry: BackupMetadata) : BackupMetadataState()

    data class Inactive(override val entry: BackupMetadata) : BackupMetadataState()
}
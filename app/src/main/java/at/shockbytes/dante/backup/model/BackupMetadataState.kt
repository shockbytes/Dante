package at.shockbytes.dante.backup.model

sealed class BackupMetadataState {

    abstract val entry: BackupMetadata

    data class Active(override val entry: BackupMetadata) : BackupMetadataState()

    data class Inactive(override val entry: BackupMetadata) : BackupMetadataState()
}
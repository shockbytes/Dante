package at.shockbytes.dante.backup.model

sealed class BackupMetadataState {

    abstract val entry: BackupMetadata

    val timestamp: Long
        get() = entry.timestamp

    data class Active(override val entry: BackupMetadata) : BackupMetadataState()

    data class Inactive(override val entry: BackupMetadata) : BackupMetadataState()

    /**
     * Object indicating that the [BackupMetadata] object does no longer exist
     */
    object Unreachable : BackupMetadataState() {
        override val entry: BackupMetadata
            get() = throw UninitializedPropertyAccessException("Entry property of Unreachable object must never be invoked!")
    }
}
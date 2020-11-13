package at.shockbytes.dante.backup.model

/**
 * Actual [content] that is written into the backup file with the given [fileName].
 */
data class BackupData(
    val fileName: String,
    val content: String
)
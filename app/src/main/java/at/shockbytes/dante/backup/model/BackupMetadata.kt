package at.shockbytes.dante.backup.model

/**
 * Author:  Martin Macheiner
 * Date:    30.04.2017
 */
data class BackupMetadata(
    val id: String,
    val fileName: String,
    val device: String,
    val storageProvider: BackupStorageProvider,
    val books: Int,
    val timestamp: Long
)

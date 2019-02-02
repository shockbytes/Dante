package at.shockbytes.dante.backup

/**
 * Author: Martin Macheiner
 * Date: 30.04.2017.
 */
data class BackupEntry(
    val fileId: String = "",
    val fileName: String = "",
    val device: String = "",
    val storageProvider: String = "",
    val books: Int = 0,
    val timestamp: Long = 0L,
    val isAutoBackup: Boolean = false
)

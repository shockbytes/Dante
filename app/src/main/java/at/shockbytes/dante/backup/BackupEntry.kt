package at.shockbytes.dante.backup

/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */

data class BackupEntry(var fileId: String = "", var fileName: String = "", var device: String = "",
                       var storageProvider: String ="", var books: Int = 0,
                       var timestamp: Long = 0L, var isAutoBackup: Boolean = false)

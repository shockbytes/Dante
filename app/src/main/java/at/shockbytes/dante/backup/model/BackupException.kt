package at.shockbytes.dante.backup.model

/**
 * Author:  Martin Macheiner
 * Date:    01.05.2017
 */
class BackupException(s: String, val fileName: String? = null) : Throwable(s)

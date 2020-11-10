package at.shockbytes.dante.backup.model

import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    30.04.2017
 */
sealed class BackupMetadata {

    abstract val id: String
    abstract val fileName: String
    abstract val device: String
    abstract val storageProvider: BackupStorageProvider
    abstract val books: Int
    abstract val timestamp: Long

    data class Standard(
        override val id: String,
        override val fileName: String,
        override val device: String,
        override val storageProvider: BackupStorageProvider,
        override val books: Int,
        override val timestamp: Long
    ) : BackupMetadata()

    data class WithLocalFile(
        override val id: String,
        override val fileName: String,
        override val device: String,
        override val storageProvider: BackupStorageProvider,
        override val books: Int,
        override val timestamp: Long,
        val localFilePath: File
    ) : BackupMetadata()

    companion object {

        fun Standard.attachLocalFile(localFile: File): WithLocalFile {
            return WithLocalFile(
                id = this.id,
                fileName = this.fileName,
                device = this.device,
                storageProvider = this.storageProvider,
                books = this.books,
                timestamp = this.timestamp,
                localFilePath = localFile
            )
        }
    }
}

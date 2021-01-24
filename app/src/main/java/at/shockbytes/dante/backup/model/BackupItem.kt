package at.shockbytes.dante.backup.model

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.PageRecord

/**
 * BackupItem holds both the metadata and the actual listBackupFiles of books
 *
 * Author:  Martin Macheiner
 * Date:    29.05.2019
 */
data class BackupItem(
    val backupMetadata: BackupMetadata.Standard,
    val books: List<BookEntity>,
    val records: List<PageRecord> = listOf()
)
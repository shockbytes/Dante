package at.shockbytes.dante.backup.model

import at.shockbytes.dante.book.BookEntity

/**
 * BackupItem holds both the metadata and the actual list of books
 *
 * Author:  Martin Macheiner
 * Date:    29.05.2019
 */
data class BackupItem(
    val backupMetadata: BackupMetadata,
    val books: List<BookEntity>
)
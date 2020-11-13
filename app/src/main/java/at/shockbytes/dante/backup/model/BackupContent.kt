package at.shockbytes.dante.backup.model

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.PageRecord

data class BackupContent(
    val books: List<BookEntity>,
    val records: List<PageRecord>
) {
    val isEmpty: Boolean
        get() = books.isEmpty()
}
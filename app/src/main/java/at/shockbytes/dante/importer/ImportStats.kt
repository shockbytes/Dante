package at.shockbytes.dante.importer

data class ImportStats(
    val importedBooks: Int,
    val readLaterBooks: Int,
    val currentlyReadingBooks: Int,
    val readBooks: Int
)

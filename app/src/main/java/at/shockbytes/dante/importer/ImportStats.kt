package at.shockbytes.dante.importer

sealed class ImportStats {

    data class Success(
        val importedBooks: Int,
        val readLaterBooks: Int,
        val currentlyReadingBooks: Int,
        val readBooks: Int
    ) : ImportStats()

    object NoBooks : ImportStats()
}

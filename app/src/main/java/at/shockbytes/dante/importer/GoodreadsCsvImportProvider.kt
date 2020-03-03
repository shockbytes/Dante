package at.shockbytes.dante.importer

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.storage.reader.CsvReader
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Single
import org.joda.time.format.DateTimeFormat

class GoodreadsCsvImportProvider(
    private val csvReader: CsvReader,
    private val schedulers: SchedulerFacade
) : ImportProvider {

    override val importer = Importer.GOODREADS_CSV

    private val goodReadsDateFormat = DateTimeFormat.forPattern("yyyy/MM/dd")

    override fun importFromContent(content: String): Single<List<BookEntity>> {
        return csvReader.readCsvContent(content)
            .subscribeOn(schedulers.io)
            .map { lines ->

                if (verifyCsvFormat(lines.firstOrNull())) {
                    lines
                        .drop(1)
                        .mapNotNull(::createBookEntityFromLine)
                        .toList()
                } else {
                    // Return a list of empty books and indicate that no books could be imported
                    // NOTE: This can be vastly improved by returning an exception
                    listOf()
                }
            }
    }

    /**
     * Verify the first and the last column in the CSV format
     */
    private fun verifyCsvFormat(initialLine: List<String>?): Boolean {
        return initialLine?.let { line ->
            line.getOrNull(TITLE_COL) == TITLE_COL_NAME &&
                line.getOrNull(NOTES_COL) == NOTES_COL_NAME
        } ?: false
    }

    private fun createBookEntityFromLine(line: List<String>): BookEntity? {

        val title = line.getOrNull(TITLE_COL) ?: ""
        val author = line.getOrNull(AUTHOR_COL) ?: ""
        val isbn = line.getOrNull(ISBN_COL) ?: ""
        val rating = line.getOrNull(RATING_COL)?.toIntOrNull() ?: 0
        val pages = line.getOrNull(PAGES_COL)?.toIntOrNull() ?: 0
        val publishedYear = line.getOrNull(PUBLISHED_COL) ?: ""
        val dateRead = line.getOrNull(DATE_READ_COL) ?: ""
        val dateAdded = line.getOrNull(DATE_ADDED_COL) ?: ""
        val shelf = line.getOrNull(SHELF_COL) ?: ""
        val notes = line.getOrNull(NOTES_COL)

        val bookState = bookStateFromShelf(shelf)
        val startDate = parseDateStringToMillis(dateAdded)
        val endDate = parseDateStringToMillis(dateRead, defaultMillis = startDate)

        return if (areMandatoryFieldsAvailable(title, author, pages)) {
            BookEntity(
                title = title,
                author = author,
                pageCount = pages,
                rating = rating,
                isbn = isbn,
                publishedDate = publishedYear,
                wishlistDate = startDate,
                startDate = startDate,
                state = bookState,
                notes = notes,
                endDate = endDate
            )
        } else {
            null
        }
    }

    private fun parseDateStringToMillis(
        date: String,
        defaultMillis: Long = System.currentTimeMillis()
    ): Long {
        return if (date.isNotEmpty()) {
            goodReadsDateFormat.parseMillis(date)
        } else {
            defaultMillis
        }
    }

    private fun bookStateFromShelf(shelf: String): BookState {
        return when (shelf) {
            "currently-reading" -> BookState.READING
            "read" -> BookState.READ
            "to-read" -> BookState.READ_LATER
            else -> BookState.READ_LATER
        }
    }

    private fun areMandatoryFieldsAvailable(title: String?, author: String?, pages: Int?): Boolean {
        return title != null && author != null && pages != null
    }

    companion object {

        private const val TITLE_COL_NAME = "Title"
        private const val NOTES_COL_NAME = "Private Notes"

        private const val TITLE_COL = 1
        private const val AUTHOR_COL = 2
        private const val ISBN_COL = 6
        private const val RATING_COL = 7
        private const val PAGES_COL = 11
        private const val PUBLISHED_COL = 12
        private const val DATE_READ_COL = 14
        private const val DATE_ADDED_COL = 15
        private const val SHELF_COL = 18
        private const val NOTES_COL = 21
    }
}
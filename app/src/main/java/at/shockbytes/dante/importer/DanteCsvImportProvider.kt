package at.shockbytes.dante.importer

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.storage.reader.CsvReader
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Single

class DanteCsvImportProvider(
    private val csvReader: CsvReader,
    private val schedulers: SchedulerFacade
) : ImportProvider {

    override val importer = Importer.DANTE_CSV

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
                    // Return a listBackupFiles of empty books and indicate that no books could be imported
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
                line.getOrNull(LABELS_COL) == LABELS_COL_NAME
        } ?: false
    }

    private fun createBookEntityFromLine(line: List<String>): BookEntity? {

        val title = line.getOrNull(TITLE_COL) ?: ""
        val subTitle = line.getOrNull(SUBTITLE_COL) ?: ""
        val author = line.getOrNull(AUTHOR_COL) ?: ""
        val isbn = line.getOrNull(ISBN_COL) ?: ""
        val rating = line.getOrNull(RATING_COL)?.toIntOrNull() ?: 0
        val pages = line.getOrNull(PAGES_COL)?.toIntOrNull() ?: 0
        val publishedYear = line.getOrNull(PUBLISHED_DATE_COL) ?: ""
        val startDate = line.getOrNull(START_DATE_COL)?.toLongOrNull() ?: System.currentTimeMillis()
        val endDate = line.getOrNull(END_DATE_COL)?.toLongOrNull() ?: System.currentTimeMillis()
        val wishlistDate = line.getOrNull(WISHLIST_COL)?.toLongOrNull()
            ?: System.currentTimeMillis()
        val state = BookState.fromString(line.getOrNull(STATE_COL))
        val notes = line.getOrNull(NOTES_COL)
        val summary = line.getOrNull(SUMMARY_COL)
        val googleBooksLink = line.getOrNull(GOOGLE_BOOKS_LINK_COL)
        val thumbnailAddress = line.getOrNull(THUMBNAIL_COL)
        val currentPage = line.getOrNull(CURRENT_PAGE_COL)?.toIntOrNull() ?: 0
        val language = line.getOrNull(LANGUAGE_COL) ?: "NA"
        val labels = line.getOrNull(LABELS_COL)?.let(::labelsFromCsv) ?: listOf()

        return if (areMandatoryFieldsAvailable(title, author, pages)) {
            BookEntity(
                title = title,
                state = state,
                subTitle = subTitle,
                author = author,
                pageCount = pages,
                rating = rating,
                isbn = isbn,
                publishedDate = publishedYear,
                wishlistDate = wishlistDate,
                startDate = startDate,
                notes = notes,
                endDate = endDate,
                summary = summary,
                googleBooksLink = googleBooksLink,
                thumbnailAddress = thumbnailAddress,
                currentPage = currentPage,
                language = language,
                labels = labels
            )
        } else {
            null
        }
    }

    private fun labelsFromCsv(labels: String): List<BookLabel> {
        return labels
            .split(";")
            .mapNotNull { label ->
                val parts = label.split(":")
                if (parts.size == 2) {
                    BookLabel.unassignedLabel(title = parts[0], hexColor = parts[1])
                } else {
                    null
                }
            }
    }

    private fun areMandatoryFieldsAvailable(title: String?, author: String?, pages: Int?): Boolean {
        return title != null && author != null && pages != null
    }

    companion object {

        private const val TITLE_COL_NAME = "title"
        private const val LABELS_COL_NAME = "labels"

        private const val TITLE_COL = 0
        private const val SUBTITLE_COL = 1
        private const val AUTHOR_COL = 2
        private const val STATE_COL = 3
        private const val PAGES_COL = 4
        private const val PUBLISHED_DATE_COL = 5
        private const val ISBN_COL = 6
        private const val THUMBNAIL_COL = 7
        private const val GOOGLE_BOOKS_LINK_COL = 8
        private const val START_DATE_COL = 9
        private const val END_DATE_COL = 10
        private const val WISHLIST_COL = 11
        private const val LANGUAGE_COL = 12
        private const val RATING_COL = 13
        private const val CURRENT_PAGE_COL = 14
        private const val NOTES_COL = 15
        private const val SUMMARY_COL = 16
        private const val LABELS_COL = 17
    }
}
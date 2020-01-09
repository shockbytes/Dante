package at.shockbytes.dante.timeline

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import org.joda.time.DateTime

object TimeLineBuilder {

    private data class MonthAndYear(
        val month: Int,
        val year: Int
    )

    fun buildTimeLineItems(books: List<BookEntity>): List<TimeLineItem> {

        val content = mutableListOf<TimeLineItem>()

        books
            .filter { it.startDate > 0 && it.state != BookState.READ_LATER }
            .sortedBy { it.startDate }
            .asReversed()
            .groupBy { book ->
                DateTime(book.startDate).run {
                    MonthAndYear(monthOfYear, year)
                }
            }
            .forEach { (monthAndYear, books) ->

                content.add(TimeLineItem.MonthHeader(monthAndYear.month, monthAndYear.year))

                books.mapTo(content) { book ->
                    TimeLineItem.BookTimeLineItem(book.title, book.thumbnailAddress)
                }
        }

        return content
    }
}
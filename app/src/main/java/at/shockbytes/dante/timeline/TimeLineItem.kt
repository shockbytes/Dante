package at.shockbytes.dante.timeline

import at.shockbytes.dante.core.book.BookId

sealed class TimeLineItem {

    data class BookTimeLineItem(
        val bookId: BookId,
        val title: String,
        val image: String?
    ) : TimeLineItem()

    data class MonthHeader(val month: Int, val year: Int) : TimeLineItem()

    object DanteInstall : TimeLineItem()
}
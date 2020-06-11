package at.shockbytes.dante.timeline

sealed class TimeLineItem {

    data class BookTimeLineItem(
        val bookId: Long,
        val title: String,
        val image: String?
    ) : TimeLineItem()

    data class MonthHeader(val month: Int, val year: Int) : TimeLineItem()

    object DanteInstall : TimeLineItem()
}
package at.shockbytes.dante.ui.adapter.pagerecords

import at.shockbytes.dante.core.book.PageRecord

data class PageRecordDetailItem(
        val pageRecord: PageRecord,
        val formattedPagesRead: String,
        val formattedDate: String
)
package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
fun BookEntity.toJson(): JsonObject {
    return JsonObject().apply {
        addProperty("position", position)
        addProperty("title", title)
        addProperty("subTitle", subTitle)
        addProperty("author", author)
        addProperty("pageCount", pageCount)
        addProperty("publishedDate", publishedDate)
        addProperty("isbn", isbn)
        addProperty("language", language)
        addProperty("currentPage", currentPage)
        addProperty("notes", notes)
        addProperty("thumbnailAddress", thumbnailAddress)
        addProperty("googleBooksLink", googleBooksLink)
        addProperty("ordinalState", state.ordinal)
        addProperty("rating", rating)
        addProperty("startDate", startDate)
        addProperty("endDate", endDate)
        addProperty("wishlistDate", wishlistDate)
        addProperty("summary", summary)
        add("labels", JsonArray().apply { labels.forEach { add(it) } })
    }
}

fun BookEntity.createSharingIntent(c: Context): Intent {
    val msg = c.getString(R.string.share_template, this.title, this.googleBooksLink)
    return Intent()
            .setAction(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_TEXT, msg)
            .setType("text/plain")
}

/**
 * The only visible update is the page count, but check the dates as well,
 * because this prevents an error of not overriding newly set dates in the main list.
 * To be sure check basically everything what can change!
 */
fun BookEntity.isContentSame(other: BookEntity): Boolean {
    return (this.id == other.id) &&
            (this.currentPage == other.currentPage) &&
            (this.pageCount == other.pageCount) &&
            (this.wishlistDate == other.wishlistDate) &&
            (this.startDate == other.startDate) &&
            (this.endDate == other.endDate) &&
            (this.rating == other.rating) &&
            (this.notes == other.notes)
}
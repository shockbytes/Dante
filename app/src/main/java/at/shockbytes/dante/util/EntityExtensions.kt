package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import com.google.gson.JsonObject

/**
 * @author Martin Macheiner
 * Date: 12-Jun-18.
 */

fun BookEntity.toJson(): JsonObject {
    val jsonObject = JsonObject()
    jsonObject.addProperty("position", position)
    jsonObject.addProperty("title", title)
    jsonObject.addProperty("subTitle", subTitle)
    jsonObject.addProperty("author", author)
    jsonObject.addProperty("pageCount", pageCount)
    jsonObject.addProperty("publishedDate", publishedDate)
    jsonObject.addProperty("isbn", isbn)
    jsonObject.addProperty("language", language)
    jsonObject.addProperty("currentPage", currentPage)
    jsonObject.addProperty("notes", notes)
    jsonObject.addProperty("thumbnailAddress", thumbnailAddress)
    jsonObject.addProperty("googleBooksLink", googleBooksLink)
    jsonObject.addProperty("ordinalState", state.ordinal)
    jsonObject.addProperty("rating", rating)
    jsonObject.addProperty("startDate", startDate)
    jsonObject.addProperty("endDate", endDate)
    jsonObject.addProperty("wishlistDate", wishlistDate)
    return jsonObject
}

fun BookEntity.createSharingIntent(c: Context): Intent {
    val msg = c.getString(R.string.share_template, this.title, this.googleBooksLink)
    return Intent()
            .setAction(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_TEXT, msg)
            .setType("text/plain")
}


// The only visible update is the page count, so just check page count
fun BookEntity.isContentSame(other: BookEntity): Boolean {
    return (this.id == other.id) && (this.currentPage == other.currentPage)
}
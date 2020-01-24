package at.shockbytes.dante.core.book.realm

import at.shockbytes.dante.util.Gsonify
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Author:  Martin Macheiner
 * Date:    27.08.2016
 */
open class RealmBook @JvmOverloads constructor(
    @PrimaryKey var id: Long = -1,
    var title: String = "",
    var subTitle: String = "",
    var author: String = "",
    var pageCount: Int = 0,
    var publishedDate: String = "",
    var position: Int = 0,
    var isbn: String = "",
    var thumbnailAddress: String? = null,
    var googleBooksLink: String? = null, // Version 1
    var startDate: Long = 0,
    var endDate: Long = 0,
    var wishlistDate: Long = 0,
    var language: String = "NA", // Version 2
    var rating: Int = 0, // 1 - 5
    var currentPage: Int = 0, // Version 3
    var notes: String? = null,
    var summary: String? = null, // Version 4-5
    var labels: RealmList<RealmBookLabel> = RealmList()
) : RealmObject(), Gsonify {

    enum class State {
        READ_LATER, READING, READ
    }

    private var ordinalState: Int = 0

    var state: State
        get() = State.values()[ordinalState]
        set(state) {
            this.ordinalState = state.ordinal
        }

    val reading: Boolean
        get() = state == State.READING

    init {
        ordinalState = State.READ_LATER.ordinal
    }

    override fun toJson(): JsonObject {
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
        }
    }

    override fun toString() = toJson().toString()
}

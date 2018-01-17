package at.shockbytes.dante.util.books

import at.shockbytes.dante.util.Gsonify
import com.google.gson.JsonObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * @author Martin Macheiner
 * Date: 27.08.2016.
 */

open class Book @JvmOverloads constructor(@PrimaryKey var id: Long = -1,
                                          var title: String = "", var subTitle: String = "",
                                          var author: String = "", var pageCount: Int = 0,
                                          var publishedDate: String = "", var position: Int = 0,
                                          var isbn: String = "", var thumbnailAddress: String? = null,
                                          var googleBooksLink: String? = null, // Version 1
                                          var startDate: Long = 0, var endDate: Long = 0,
                                          var wishlistDate: Long = 0, var language: String = "NA", // Version 2
                                          var rating: Int = 0, // 1 - 5
                                          var currentPage: Int = 0, // Version 3
                                          var notes: String? = null) : RealmObject(), Gsonify {

    enum class State {
        READ_LATER, READING, READ
    }

    private var ordinalState: Int = 0

    var state: State
        get() = State.values()[ordinalState]
        set(state) {
            this.ordinalState = state.ordinal

            when (state) {
                Book.State.READ_LATER -> {
                    wishlistDate = System.currentTimeMillis()
                    startDate = 0
                    endDate = 0
                }
                Book.State.READING -> {
                    startDate = System.currentTimeMillis()
                    endDate = 0
                }
                Book.State.READ -> {
                    endDate = System.currentTimeMillis()
                    if (startDate == 0L) {
                        startDate = endDate
                    }
                }
            }
        }

    val reading: Boolean
        get() = state == State.READING

    val hasPages: Boolean
        get() = pageCount > 0

    val isAnyTimeInformationAvailable: Boolean
        get() = wishlistDate != 0L || startDate != 0L || endDate != 0L

    init {
        ordinalState = State.READ_LATER.ordinal
    }

    override fun toJson(): JsonObject {
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

    override fun toString() = toJson().toString()

}

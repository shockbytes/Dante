package at.shockbytes.dante.network.google.gson

import at.shockbytes.dante.books.BookFactory
import at.shockbytes.dante.books.BookSuggestion
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import com.google.gson.*
import java.lang.reflect.Type


/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

class GoogleBooksSuggestionResponseDeserializer : JsonDeserializer<BookSuggestion> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type,
                             context: JsonDeserializationContext): BookSuggestion? {

        if (json.asJsonObject.getAsJsonArray("items") != null) {

            var size = json.asJsonObject.getAsJsonArray("items").size()
            if (size > 0) {

                // Retrieve the main information from the json object
                val volumeInfoMain = json.asJsonObject.getAsJsonArray("items").get(0)
                        .asJsonObject.get("volumeInfo").asJsonObject

                // Look for main suggestion and check for fetching size
                val mainSuggestion = grabBook(volumeInfoMain)
                size = if (size >= DanteUtils.maxFetchAmount) DanteUtils.maxFetchAmount else size

                // Because the data of other books is already fetched, let's convert them into objects
                val otherSuggestions = (1 until size).map { idx ->
                    val volumeInfo = json.asJsonObject.getAsJsonArray("items")
                            .get(idx).asJsonObject.get("volumeInfo").asJsonObject
                    grabBook(volumeInfo)
                }
                return BookSuggestion(mainSuggestion, otherSuggestions)
            }
        }
        return null
    }

    private fun grabBook(volumeInfo: JsonObject): Book {

        val title = volumeInfo.get("title").asString
        val subtitle = getSubtitle(volumeInfo)
        val author = getAuthors(volumeInfo)
        val pageCount = getPageCount(volumeInfo)
        val publishedDate = getPublishedDate(volumeInfo)
        val isbn = getIsbn(volumeInfo)
        val thumbnailAddress = getImageLink(volumeInfo)
        val googleBooksLink = getGoogleBooksLink(volumeInfo)
        val language = getLanguage(volumeInfo)

        return BookFactory.resolve(title, subtitle, author, pageCount, publishedDate, isbn,
                thumbnailAddress, googleBooksLink, language)
    }

    private fun getPublishedDate(volumeInfo: JsonObject): String {
        return if (volumeInfo.get("publishedDate") != null) {
            volumeInfo.get("publishedDate").asString
        } else ""
    }

    private fun getGoogleBooksLink(volumeInfo: JsonObject): String {
        return if (volumeInfo.get("infoLink") != null) {
            volumeInfo.get("infoLink").asString
        } else ""
    }

    private fun getLanguage(volumeInfo: JsonObject): String {
        return if (volumeInfo.get("language") != null) {
            volumeInfo.get("language").asString
        } else ""
    }

    private fun getSubtitle(volumeInfo: JsonObject): String {
        return if (volumeInfo.get("subtitle") != null) {
            volumeInfo.get("subtitle").asString
        } else ""
    }

    private fun getPageCount(volumeInfo: JsonObject): Int {
        return if (volumeInfo.get("pageCount") != null) {
            volumeInfo.get("pageCount").asInt
        } else 0
    }

    private fun getIsbn(volumeInfo: JsonObject): String {
        if (volumeInfo.get("industryIdentifiers") != null) {
            val allIsbn = volumeInfo.get("industryIdentifiers").asJsonArray
            allIsbn
                    .map { it.asJsonObject }
                    .filter { it.get("type") != null && it.get("type").asString == "ISBN_13" }
                    .forEach { return it.get("identifier").asString }
        }
        return ""
    }

    private fun getAuthors(volumeInfo: JsonObject): String {
        return if (volumeInfo.get("authors") != null) {
            val authors = volumeInfo.get("authors").asJsonArray
            authors.joinToString(", ") { it.asString }
        } else {
            ""
        }
    }

    private fun getImageLink(volumeInfo: JsonObject): String? {
        return if (volumeInfo.get("imageLinks") != null) {
            volumeInfo.get("imageLinks").asJsonObject
                    .get("thumbnail").asString
        } else null
    }

}

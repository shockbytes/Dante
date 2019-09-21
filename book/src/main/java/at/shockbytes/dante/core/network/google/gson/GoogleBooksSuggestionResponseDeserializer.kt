package at.shockbytes.dante.core.network.google.gson

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookSuggestion
import at.shockbytes.dante.core.network.BookDownloader.Companion.MAX_FETCH_AMOUNT
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
class GoogleBooksSuggestionResponseDeserializer : JsonDeserializer<BookSuggestion> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BookSuggestion? {

        return json.asJsonObject.getAsJsonArray("items")?.let {

            var size = json.asJsonObject.getAsJsonArray("items").size()
            if (size > 0) {

                // Retrieve the main information from the json object
                val volumeInfoMain = json.asJsonObject.getAsJsonArray("items").get(0)
                        .asJsonObject.get("volumeInfo").asJsonObject

                // Look for main suggestion and check for fetching size
                val mainSuggestion = grabBook(volumeInfoMain)
                size = if (size >= MAX_FETCH_AMOUNT) MAX_FETCH_AMOUNT else size

                // Because the data of other books is already fetched, let's convert them into objects
                val otherSuggestions = (1 until size).map { idx ->
                    val volumeInfo = json.asJsonObject.getAsJsonArray("items")
                            .get(idx).asJsonObject.get("volumeInfo").asJsonObject
                    grabBook(volumeInfo)
                }
                BookSuggestion(mainSuggestion, otherSuggestions)
            } else null
        }
    }

    private fun grabBook(volumeInfo: JsonObject): BookEntity {

        val title = volumeInfo.get("title").asString
        val subtitle = getSubtitle(volumeInfo)
        val author = getAuthors(volumeInfo)
        val pageCount = getPageCount(volumeInfo)
        val publishedDate = getPublishedDate(volumeInfo)
        val isbn = getIsbn(volumeInfo)
        val thumbnailAddress = getImageLink(volumeInfo)
        val googleBooksLink = getGoogleBooksLink(volumeInfo)
        val language = getLanguage(volumeInfo)
        val summary = getSummary(volumeInfo)

        return BookEntity(
            title = title,
            subTitle = subtitle,
            author = author,
            pageCount = pageCount,
            publishedDate = publishedDate,
            isbn = isbn,
            thumbnailAddress = thumbnailAddress,
            googleBooksLink = googleBooksLink,
            language = language,
            summary = summary
        )
    }

    private fun getPublishedDate(volumeInfo: JsonObject): String {
        return volumeInfo.get("publishedDate")?.asString ?: ""
    }

    private fun getGoogleBooksLink(volumeInfo: JsonObject): String {
        return volumeInfo.get("infoLink")?.asString ?: ""
    }

    private fun getLanguage(volumeInfo: JsonObject): String {
        return volumeInfo.get("language")?.asString ?: ""
    }

    private fun getSubtitle(volumeInfo: JsonObject): String {
        return volumeInfo.get("subtitle")?.asString ?: ""
    }

    private fun getPageCount(volumeInfo: JsonObject): Int {
        return volumeInfo.get("pageCount")?.asInt ?: 0
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
        return volumeInfo.get("authors")?.let { authors ->
            authors.asJsonArray
                .joinToString(", ") { it.asString }
        } ?: ""
    }

    private fun getImageLink(volumeInfo: JsonObject): String? {
        return volumeInfo.get("imageLinks")?.asJsonObject?.get("thumbnail")?.asString
    }

    private fun getSummary(volumeInfo: JsonObject): String? {
        return volumeInfo.get("description")?.asString
    }
}

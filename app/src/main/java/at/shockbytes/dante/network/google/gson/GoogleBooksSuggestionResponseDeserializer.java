package at.shockbytes.dante.network.google.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import at.shockbytes.dante.util.AppParams;
import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.books.BookSuggestion;


/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public class GoogleBooksSuggestionResponseDeserializer implements JsonDeserializer<BookSuggestion> {

    @Override
    public BookSuggestion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (json.getAsJsonObject().getAsJsonArray("items") != null) {

            int size = json.getAsJsonObject().getAsJsonArray("items").size();
            if (size > 0) {

                JsonObject volumeInfoMain = json.getAsJsonObject().getAsJsonArray("items").get(0)
                        .getAsJsonObject().get("volumeInfo").getAsJsonObject();

                Book mainSuggestion = grabBook(volumeInfoMain);
                List<Book> otherSuggestions = new ArrayList<>();

                size = (size >= AppParams.INSTANCE.getMaxFetchAmount())
                        ? AppParams.INSTANCE.getMaxFetchAmount()
                        : size;
                for (int i = 1; i < size; i++) {
                    JsonObject volumeInfo = json.getAsJsonObject().getAsJsonArray("items").get(i)
                            .getAsJsonObject().get("volumeInfo").getAsJsonObject();
                    otherSuggestions.add(grabBook(volumeInfo));
                }

                return new BookSuggestion(mainSuggestion, otherSuggestions);
            }
        }
        return null;
    }

    private Book grabBook(JsonObject volumeInfo) {

        String title = volumeInfo.get("title").getAsString();
        String subtitle = getSubtitle(volumeInfo);
        String author = getAuthors(volumeInfo);
        int pageCount = getPageCount(volumeInfo);
        String publishedDate = getPublishedDate(volumeInfo);
        String isbn = getIsbn(volumeInfo);
        String thumbnailAddress = getImageLink(volumeInfo);
        String googleBooksLink = getGoogleBooksLink(volumeInfo);
        String language = getLanguage(volumeInfo);

        return new Book(title, subtitle, author, pageCount, publishedDate,
                isbn, thumbnailAddress, googleBooksLink, language);
    }

    private String getPublishedDate(JsonObject volumeInfo) {
        if (volumeInfo.get("publishedDate") != null) {
            return volumeInfo.get("publishedDate").getAsString();
        }
        return "";
    }

    private String getGoogleBooksLink(JsonObject volumeInfo) {
        if (volumeInfo.get("infoLink") != null) {
            return volumeInfo.get("infoLink").getAsString();
        }
        return "";
    }

    private String getLanguage(JsonObject volumeInfo) {
        if (volumeInfo.get("language") != null) {
            return volumeInfo.get("language").getAsString();
        }
        return "";
    }

    private String getSubtitle(JsonObject volumeInfo) {

        if (volumeInfo.get("subtitle") != null) {
            return volumeInfo.get("subtitle").getAsString();
        }
        return "";
    }

    private int getPageCount(JsonObject volumeInfo) {
        if (volumeInfo.get("pageCount") != null) {
            return volumeInfo.get("pageCount").getAsInt();
        }
        return 0;
    }

    private String getIsbn(JsonObject volumeInfo) {

        if (volumeInfo.get("industryIdentifiers") != null) {
            JsonArray isbns = volumeInfo.get("industryIdentifiers").getAsJsonArray();
            for (JsonElement je : isbns) {
                JsonObject j = je.getAsJsonObject();
                if (j.get("type") != null && j.get("type").getAsString().equals("ISBN_13")) {
                    return j.get("identifier").getAsString();
                }
            }
        }
        return "";
    }

    private String getAuthors(JsonObject volumeInfo) {

        StringBuilder sb = new StringBuilder();
        if (volumeInfo.get("authors") != null) {
            JsonArray authors = volumeInfo.get("authors").getAsJsonArray();
            for (int i = 0; i < authors.size(); i++) {
                sb.append(authors.get(i).getAsString());
                if (i < authors.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }

    private String getImageLink(JsonObject volumeInfo) {
        if (volumeInfo.get("imageLinks") != null) {
            return volumeInfo.get("imageLinks").getAsJsonObject()
                    .get("thumbnail").getAsString();
        }
        return null;
    }

}

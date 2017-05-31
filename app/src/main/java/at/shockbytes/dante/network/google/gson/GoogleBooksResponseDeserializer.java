package at.shockbytes.dante.network.google.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import at.shockbytes.dante.util.books.Book;


/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public class GoogleBooksResponseDeserializer implements JsonDeserializer<Book> {

    @Override
    public Book deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        int totalItems = json.getAsJsonObject().get("totalItems").getAsInt();
        //Log.wtf("Dante", "Total items: " + totalItems);
        if (totalItems > 0) {

            /* Just for debugging
            try {
                for (int i = 0; i < totalItems; i++) {
                    String title = json.getAsJsonObject().getAsJsonArray("items").get(i)
                            .getAsJsonObject().get("volumeInfo").getAsJsonObject().get("title").getAsString();
                    Log.wtf("Dante", title);
                }
            } catch (IndexOutOfBoundsException ie) {
                ie.printStackTrace();
            } */

            JsonObject volumeInfo = json.getAsJsonObject().getAsJsonArray("items").get(0)
                    .getAsJsonObject().get("volumeInfo").getAsJsonObject();

            //Log.wtf("Dante", volumeInfo.toString());


            String title = volumeInfo.get("title").getAsString();
            String subtitle = "";
            if (volumeInfo.get("subtitle") != null) {
                subtitle = volumeInfo.get("subtitle").getAsString();
            }
            String author = getAuthors(volumeInfo);
            int pageCount = 0;
            if (volumeInfo.get("pageCount") != null) {
                pageCount = volumeInfo.get("pageCount").getAsInt();
            }
            String publishedDate = volumeInfo.get("publishedDate").getAsString();
            String isbn = getIsbn(volumeInfo);
            String thumbnailAddress = getImageLink(volumeInfo);
            String gooogleBooksLink = volumeInfo.get("infoLink").getAsString();

            return new Book(title, subtitle, author, pageCount, publishedDate,
                    isbn, thumbnailAddress, gooogleBooksLink);
        }
        return null;
    }

    private String getIsbn(JsonObject volumeInfo) {

        JsonArray isbns = volumeInfo.get("industryIdentifiers").getAsJsonArray();
        for (JsonElement je : isbns) {

            JsonObject j = je.getAsJsonObject();
            if (j.get("type").getAsString().equals("ISBN_13")) {
                return j.get("identifier").getAsString();
            }
        }

        return "";
    }

    private String getAuthors(JsonObject volumeInfo) {

        StringBuilder sb = new StringBuilder();
        JsonArray authors = volumeInfo.get("authors").getAsJsonArray();
        for (int i = 0; i < authors.size(); i++) {
            sb.append(authors.get(i).getAsString());
            if (i < authors.size() - 1) {
                sb.append(", ");
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

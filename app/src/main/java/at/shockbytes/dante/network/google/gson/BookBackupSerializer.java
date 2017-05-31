package at.shockbytes.dante.network.google.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import at.shockbytes.dante.util.books.Book;


/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public class BookBackupSerializer implements JsonSerializer<Book> {


    @Override
    public JsonElement serialize(Book src, Type typeOfSrc, JsonSerializationContext context) {
        return src.toJson();
    }


}

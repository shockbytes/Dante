package at.shockbytes.dante.network.google.gson

import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.util.toJson
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type


/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

class BookBackupSerializer : JsonSerializer<BookEntity> {

    override fun serialize(src: BookEntity, typeOfSrc: Type,
                           context: JsonSerializationContext): JsonElement = src.toJson()

}

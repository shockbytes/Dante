package at.shockbytes.dante.core.network.google.gson

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.toJson
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
class BookBackupSerializer : JsonSerializer<BookEntity> {

    override fun serialize(
        src: BookEntity,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement = src.toJson()
}

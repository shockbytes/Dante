package at.shockbytes.dante.util

import com.google.gson.JsonObject

/**
 * @author Martin Macheiner
 * Date: 14.01.2018.
 */

interface Gsonify {

    fun toJson(): JsonObject
}
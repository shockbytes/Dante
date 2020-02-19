package at.shockbytes.dante.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

/**
 * @author Martin Macheiner
 * Date: 14.01.2018.
 */

inline fun <reified T> Gson.fromJson(json: String): T = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

interface Gsonify {

    fun toJson(): JsonObject
}
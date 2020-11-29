package at.shockbytes.dante.util

import android.content.Context
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader

object Assets {

    fun readFile(context: Context, fileName: String): Single<String> {
        return singleOf(Schedulers.io()) {
            context.assets.open(fileName).use { inputStream ->
                val reader = BufferedReader(inputStream.reader())
                reader.readText()
            }
        }
    }
}
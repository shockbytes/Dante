package at.shockbytes.dante.util

import android.content.Context
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
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
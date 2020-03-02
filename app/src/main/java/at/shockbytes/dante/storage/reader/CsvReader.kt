package at.shockbytes.dante.storage.reader

import io.reactivex.Single
import java.io.File

class CsvReader : FileReader {

    private val csvRegex = (",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*\$)".toRegex())

    override fun readFile(file: File): Sequence<String> {
        return file.useLines { it }
    }

    override fun readWholeFile(file: File): Single<String> {
        return Single.fromCallable {
            file.readLines().joinToString("\n")
        }
    }

    fun readCsvContent(content: String): Single<Sequence<List<String>>> {
        return Single.fromCallable {
            content
                .split("\n")
                .asSequence()
                .map { line ->
                    line
                        .split(csvRegex)
                        .map { it.trim() }
                }
        }
    }
}
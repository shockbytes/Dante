package at.shockbytes.dante.storage.reader

import io.reactivex.Single
import java.io.File

class CsvReader : FileReader {

    override fun readFile(file: File): Sequence<String> {
        return file.useLines { it }
    }

    override fun readWholeFile(file: File): Single<String> {
        return Single.fromCallable {
            file.readLines().joinToString("\n")
        }
    }

    fun readCsvFile(file: File): Single<Sequence<List<String>>> {
        return Single.fromCallable {
            readFile(file).map { line ->
                line
                    .split(",")
                    .map { it.trim() }
            }
        }
    }
}
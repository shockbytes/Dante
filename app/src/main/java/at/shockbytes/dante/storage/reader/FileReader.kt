package at.shockbytes.dante.storage.reader

import io.reactivex.rxjava3.core.Single
import java.io.File

interface FileReader {

    fun readFile(file: File): Sequence<String>

    fun readWholeFile(file: File): Single<String>
}
package at.shockbytes.dante.storage

import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    11.06.2019
 */
interface ExternalStorageInteractor {

    @Throws(IllegalStateException::class)
    fun createBaseDirectory(directoryName: String)

    fun writeToFileInDirectory(
        directoryName: String,
        fileName: String,
        content: String
    ): Completable

    fun deleteFilesInDirectory(directoryName: String): Completable

    fun <T> transformFilesInDirectory(
        directoryName: String,
        filterPredicate: (name: String) -> Boolean,
        mapFunction: (file: File) -> T
    ): Single<List<T>>

    fun <T> transformFileContent(
        directoryName: String,
        fileName: String,
        transformFun: (content: String) -> T
    ): T

    fun deleteFileInDirectory(directoryName: String, fileName: String): Completable
}
package at.shockbytes.dante.storage

import android.os.Environment
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    11.06.2019
 */
class DefaultExternalStorageInteractor : ExternalStorageInteractor {

    override fun createBaseDirectory(directoryName: String) {

        val baseFile = getBaseFile(directoryName)

        if (!baseFile.mkdirs()) {
            throw IllegalStateException("Cannot create baseFile in $baseFile")
        }
    }

    override fun writeToFileInDirectory(directoryName: String, fileName: String, content: String): Completable {
        return Completable.fromCallable {
            File(getBaseFile(directoryName), fileName).run {

                if (!exists()) {
                    if (!createNewFile()) {
                        throw IllegalStateException("Cannot create new file at location $absolutePath")
                    }
                }

                writeText(content)
            }
        }
    }

    override fun deleteFilesInDirectory(directoryName: String): Completable {
        return Completable.fromAction {
            getBaseFile(directoryName)
                .listFiles()
                .forEach { f ->
                    f.delete()
                }
        }
    }

    override fun deleteFileInDirectory(directoryName: String, fileName: String): Completable {
        return Completable.fromAction {

            val file = File(getBaseFile(directoryName), fileName)
            if (file.exists()) {
                if (!file.delete()) {
                    throw IllegalStateException("File $fileName cannot be deleted!")
                }
            } else {
                throw NullPointerException("File associated to $fileName does not exist!")
            }
        }
    }

    override fun listFilesInDirectory(
        directoryName: String,
        filterPredicate: (name: String) -> Boolean
    ): Single<List<File>> {
        return Single.fromCallable {

            getBaseFile(directoryName)
                .listFiles { _, name ->
                    filterPredicate(name)
                }
                .toList()
        }
    }

    override fun readFileContent(
        directoryName: String,
        fileName: String
    ): String {
        val file = File(getBaseFile(directoryName), fileName)
        return file.readLines().joinToString(System.lineSeparator())
    }

    private fun getBaseFile(directoryName: String): File {
        return File(Environment.getExternalStorageDirectory(), directoryName)
    }
}
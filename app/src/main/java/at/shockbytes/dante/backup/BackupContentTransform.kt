package at.shockbytes.dante.backup

import android.os.Build
import at.shockbytes.dante.backup.model.BackupContent
import at.shockbytes.dante.backup.model.BackupData
import at.shockbytes.dante.backup.model.BackupItem
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.util.singleOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Single

class BackupContentTransform(
    private val backupStorageProvider: BackupStorageProvider,
    private val fileNameSupplier: (timestamp: Long, books: Int) -> String
) {

    private val gson: Gson = Gson()

    fun createActualBackupData(backupContent: BackupContent): Single<BackupData> {
        return singleOf {
            val timestamp = System.currentTimeMillis()
            val fileName = fileNameSupplier(timestamp, backupContent.books.size)
            val metadata = bundleMetadataForStorage(backupContent.books.size, fileName, timestamp)

            val item = BackupItem(metadata, backupContent.books, backupContent.records)
            val content = gson.toJson(item)

            BackupData(fileName, content)
        }
    }

    private fun bundleMetadataForStorage(
        books: Int,
        fileName: String,
        timestamp: Long
    ): BackupMetadata.Standard {
        return BackupMetadata.Standard(
            id = fileName,
            fileName = fileName,
            timestamp = timestamp,
            books = books,
            storageProvider = backupStorageProvider,
            device = Build.MODEL
        )
    }

    fun createBackupContentFromBackupData(content: String): Single<BackupContent> {
        return singleOf {
            gson.fromJson(content, object : TypeToken<BackupContent>() {}.type)
        }
    }
}
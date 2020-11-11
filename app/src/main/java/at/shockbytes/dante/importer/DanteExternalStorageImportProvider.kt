package at.shockbytes.dante.importer

import at.shockbytes.dante.backup.model.BackupItem
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.util.singleOf
import com.google.gson.Gson
import io.reactivex.Single

class DanteExternalStorageImportProvider(private val gson: Gson) : ImportProvider {

    override val importer: Importer = Importer.DANTE_EXTERNAL_STORAGE

    override fun importFromContent(content: String): Single<List<BookEntity>> {
        return singleOf {
            gson.fromJson(content, BackupItem::class.java).books
        }
    }
}
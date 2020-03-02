package at.shockbytes.dante.importer

import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.Single

interface ImportProvider {

    val importer: Importer

    fun importFromContent(content: String): Single<List<BookEntity>>
}
package at.shockbytes.dante.importer

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Single

class DefaultImportRepository(
    private val importProvider: Array<ImportProvider>,
    private val bookRepository: BookRepository,
    private val schedulers: SchedulerFacade
) : ImportRepository {

    private var parsedBooks: List<BookEntity>? = null

    override fun parse(importer: Importer, content: String): Single<ImportStats> {
        val provider = findImportProvider(importer)

        return provider.importFromContent(content)
            .subscribeOn(schedulers.io)
            .doOnSuccess(::cacheBooks)
            .map { books ->

                val readBooks = books.filter { it.state == BookState.READ }.count()
                val readingBooks = books.filter { it.state == BookState.READING }.count()
                val readLaterBooks = books.filter { it.state == BookState.READ_LATER }.count()

                if (books.isNotEmpty()) {
                    ImportStats.Success(
                        importedBooks = books.size,
                        readBooks = readBooks,
                        currentlyReadingBooks = readingBooks,
                        readLaterBooks = readLaterBooks
                    )
                } else {
                    ImportStats.NoBooks
                }
            }
    }

    override fun import(): Completable {
        return if (parsedBooks != null) {
            val sources = parsedBooks!!.map(bookRepository::create)
            Completable.merge(sources)
        } else {
            Completable.error(IllegalStateException("No books parsed!"))
        }
    }

    private fun cacheBooks(books: List<BookEntity>) {
        parsedBooks = books
    }

    private fun findImportProvider(importer: Importer): ImportProvider {
        return importProvider.find { it.importer == importer }
            ?: throw IllegalStateException("No ImportProvider associated to ${importer.name}")
    }
}
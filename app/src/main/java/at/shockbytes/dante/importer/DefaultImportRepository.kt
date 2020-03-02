package at.shockbytes.dante.importer

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Single
import java.io.File

class DefaultImportRepository(
    private val importProvider: Array<ImportProvider>,
    private val bookRepository: BookRepository,
    private val schedulers: SchedulerFacade
) : ImportRepository {

    override fun import(importer: Importer, file: File): Single<ImportStats> {
        val provider = findImportProvider(importer)

        return provider.importFromFile(file)
            .subscribeOn(schedulers.io)
            .doOnSuccess(::storeBooks)
            .map { books ->

                val readBooks = books.filter { it.state == BookState.READ }.count()
                val readingBooks = books.filter { it.state == BookState.READING }.count()
                val readLaterBooks = books.filter { it.state == BookState.READ_LATER }.count()

                ImportStats(
                    importedBooks = books.size,
                    readBooks = readBooks,
                    currentlyReadingBooks = readingBooks,
                    readLaterBooks = readLaterBooks
                )
            }
    }

    private fun storeBooks(books: List<BookEntity>) {
        books.forEach(bookRepository::create)
    }

    private fun findImportProvider(importer: Importer): ImportProvider {
        return importProvider.find { it.importer == importer }
            ?: throw IllegalStateException("No ImportProvider associated to ${importer.name}")
    }
}
package at.shockbytes.dante.books

import at.shockbytes.dante.util.books.Book

/**
 * @author Martin Macheiner
 * Date: 29.08.2016.
 */

interface BookListener {

    fun onBookAdded(book: Book)

    fun onBookDeleted(book: Book)

    fun onBookStateChanged(book: Book, state: Book.State)
}

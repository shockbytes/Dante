package at.shockbytes.dante.util.books

/**
 * @author Martin Macheiner
 * Date: 29.08.2016.
 */

interface BookListener {

    fun onBookAdded(book: Book)

    fun onBookDeleted(book: Book)

    fun onBookStateChanged(book: Book, state: Book.State)
}

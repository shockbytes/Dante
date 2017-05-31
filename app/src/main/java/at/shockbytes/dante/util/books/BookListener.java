package at.shockbytes.dante.util.books;

/**
 * @author Martin Macheiner
 *         Date: 29.08.2016.
 */
public interface BookListener {

    void onBookAdded(Book book);

    void onBookDeleted(Book book);

    void onBookStateChanged(Book book, Book.State state);
}

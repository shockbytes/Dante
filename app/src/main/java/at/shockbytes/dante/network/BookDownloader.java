package at.shockbytes.dante.network;

import at.shockbytes.dante.util.books.Book;
import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public interface BookDownloader {

    Observable<Book> downloadBook(String isbn);

}

package at.shockbytes.dante.network;

import at.shockbytes.dante.util.books.BookSuggestion;
import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public interface BookDownloader {

    Observable<BookSuggestion> downloadBookSuggestion(String isbn);

}

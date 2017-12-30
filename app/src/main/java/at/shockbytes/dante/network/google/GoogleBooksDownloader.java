package at.shockbytes.dante.network.google;

import javax.inject.Inject;

import at.shockbytes.dante.network.BookDownloader;
import at.shockbytes.dante.util.books.BookSuggestion;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public class GoogleBooksDownloader implements BookDownloader {

    private GoogleBooksApi api;

    @Inject
    public GoogleBooksDownloader(GoogleBooksApi api) {
        this.api = api;
    }

    @Override
    public Observable<BookSuggestion> downloadBookSuggestion(String isbn) {
        return api.downloadBookSuggestion(isbn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }
}

package at.shockbytes.dante.network.amazon;

import javax.inject.Inject;

import at.shockbytes.dante.network.BookDownloader;
import at.shockbytes.dante.util.books.BookSuggestion;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public class AmazonBookDownloader implements BookDownloader {

    private final String ACCESS_KEY = "TODO";
    private final String ASSOCIATE_TAG = "TODO";

    private AmazonItemLookupApi api;

    @Inject
    public AmazonBookDownloader(AmazonItemLookupApi api) {
        this.api = api;
    }

    @Override
    public Observable<BookSuggestion> downloadBookSuggestion(String isbn) {
        return api.downloadBookSuggestion("AWSECommerceService", "ItemLookup", "Large",
                "All", "ISBN", isbn, ACCESS_KEY, ASSOCIATE_TAG, createTimestamp(),
                createSignature())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    private String createTimestamp() {
        return null;
    }

    private String createSignature() {
        return null;
    }

}

package at.shockbytes.dante.network.amazon;

import at.shockbytes.dante.util.books.BookSuggestion;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public interface AmazonItemLookupApi {

    String SERVICE_ENDPOINT = "http://webservices.amazon.com/onca/";

    @GET("json")
    Observable<BookSuggestion> downloadBookSuggestion(@Query("Service") String service,
                                                      @Query("Operation") String operation,
                                                      @Query("ResponseGroup") String respGroup,
                                                      @Query("SearchIndex") String searchIndex,
                                                      @Query("IdType") String idType,
                                                      @Query("ItemId") String isbn,
                                                      @Query("AWSAccessKeyId") String accessKey,
                                                      @Query("AssociateTag") String associateTag,
                                                      @Query("Timestamp") String timestamp,
                                                      @Query("Signature") String requestSignature);

}

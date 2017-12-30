package at.shockbytes.dante.dagger;

import android.app.Application;

import com.google.gson.Gson;

import javax.inject.Singleton;

import at.shockbytes.dante.network.BookDownloader;
import at.shockbytes.dante.network.amazon.AmazonItemLookupApi;
import at.shockbytes.dante.network.google.GoogleBooksApi;
import at.shockbytes.dante.network.google.GoogleBooksDownloader;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Martin Macheiner
 *         Date: 19.01.2017.
 */

@Module
public class NetworkModule {

    private Application app;

    public NetworkModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //clientBuilder.addInterceptor(loggingInterceptor);

        return clientBuilder.build();
    }

    @Provides
    @Singleton
    public GoogleBooksApi provideGoogleBooksApi(OkHttpClient client, Gson gson) {

        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(GoogleBooksApi.SERVICE_ENDPOINT)
                .build()
                .create(GoogleBooksApi.class);
    }


    @Provides
    @Singleton
    public AmazonItemLookupApi provideAmazonItemlookupApi(OkHttpClient client) {

        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(AmazonItemLookupApi.SERVICE_ENDPOINT)
                .build()
                .create(AmazonItemLookupApi.class);
    }

    @Provides
    @Singleton
    public BookDownloader provideBookDownloader(GoogleBooksApi api) {
        return new GoogleBooksDownloader(api);
    }

}

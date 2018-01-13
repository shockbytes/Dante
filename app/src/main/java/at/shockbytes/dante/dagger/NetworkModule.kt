package at.shockbytes.dante.dagger

import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.network.BookDownloader
import at.shockbytes.dante.network.amazon.AmazonItemLookupApi
import at.shockbytes.dante.network.google.GoogleBooksApi
import at.shockbytes.dante.network.google.GoogleBooksDownloader
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * @author Martin Macheiner
 * Date: 19.01.2017.
 */

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {

        val clientBuilder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(loggingInterceptor)
        }
        return clientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideGoogleBooksApi(client: OkHttpClient,
                              @Named("gsonDownload") gson: Gson): GoogleBooksApi {
        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(GoogleBooksApi.SERVICE_ENDPOINT)
                .build()
                .create(GoogleBooksApi::class.java)
    }


    @Provides
    @Singleton
    fun provideAmazonItemLookupApi(client: OkHttpClient): AmazonItemLookupApi {
        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(AmazonItemLookupApi.SERVICE_ENDPOINT)
                .build()
                .create(AmazonItemLookupApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBookDownloader(api: GoogleBooksApi): BookDownloader {
        return GoogleBooksDownloader(api)
    }

}

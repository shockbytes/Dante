package at.shockbytes.dante.core.injection

import at.shockbytes.dante.core.BuildConfig
import at.shockbytes.dante.core.book.BookSuggestion
import at.shockbytes.dante.core.network.google.GoogleBooksApi
import at.shockbytes.dante.core.network.google.gson.GoogleBooksSuggestionResponseDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
 * Author:  Martin Macheiner
 * Date:    19.01.2017
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
    @Named("gsonDownload")
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(BookSuggestion::class.java, GoogleBooksSuggestionResponseDeserializer())
            .create()
    }

    @Provides
    @Singleton
    fun provideGoogleBooksApi(
        client: OkHttpClient,
        @Named("gsonDownload") gson: Gson
    ): GoogleBooksApi {
        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(GoogleBooksApi.SERVICE_ENDPOINT)
                .build()
                .create(GoogleBooksApi::class.java)
    }
}

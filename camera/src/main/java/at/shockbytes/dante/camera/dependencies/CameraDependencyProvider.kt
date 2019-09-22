package at.shockbytes.dante.camera.dependencies

import at.shockbytes.dante.camera.BuildConfig
import at.shockbytes.dante.core.book.BookSuggestion
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.core.network.google.GoogleBooksApi
import at.shockbytes.dante.core.network.google.GoogleBooksDownloader
import at.shockbytes.dante.core.network.google.gson.GoogleBooksSuggestionResponseDeserializer
import at.shockbytes.dante.util.scheduler.AppSchedulerFacade
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This class is a mess!
 * TODO Use dagger for multiple modules instead
 */
object CameraDependencyProvider {

    fun provideBooksDownloader(): BookDownloader {
        return GoogleBooksDownloader(provideGoogleBooksApi(provideOkHttpClient(), provideGson()), provideSchedulers())
    }

    fun provideSchedulers(): SchedulerFacade {
        return AppSchedulerFacade()
    }

    private fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(BookSuggestion::class.java, GoogleBooksSuggestionResponseDeserializer())
            .create()
    }

    private fun provideGoogleBooksApi(
        client: OkHttpClient,
        gson: Gson
    ): GoogleBooksApi {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(GoogleBooksApi.SERVICE_ENDPOINT)
            .build()
            .create(GoogleBooksApi::class.java)
    }

    private fun provideOkHttpClient(): OkHttpClient {

        val clientBuilder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(loggingInterceptor)
        }
        return clientBuilder.build()
    }
}
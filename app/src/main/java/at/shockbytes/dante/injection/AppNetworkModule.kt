package at.shockbytes.dante.injection

import at.shockbytes.dante.backup.provider.shockbytes.api.ShockbytesHerokuApi
import at.shockbytes.dante.core.BuildConfig
import at.shockbytes.dante.suggestions.firebase.FirebaseSuggestionsApi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class AppNetworkModule {

    private fun provideOkHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(loggingInterceptor)
        }
        return clientBuilder.build()
    }

    @Provides
    fun provideShockbytesHerokuApi(): ShockbytesHerokuApi {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ShockbytesHerokuApi.SERVICE_ENDPOINT)
            .build()
            .create(ShockbytesHerokuApi::class.java)
    }

    @Provides
    fun provideFirebaseSuggestionApi(): FirebaseSuggestionsApi {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(FirebaseSuggestionsApi.BASE_URL)
            .build()
            .create(FirebaseSuggestionsApi::class.java)
    }
}
package at.shockbytes.dante.injection

import at.shockbytes.dante.backup.provider.shockbytes.api.ShockbytesHerokuApi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class AppNetworkModule {

    @Provides
    fun provideShockbytesHerokuApi(): ShockbytesHerokuApi {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ShockbytesHerokuApi.SERVICE_ENDPOINT)
            .build()
            .create(ShockbytesHerokuApi::class.java)
    }
}
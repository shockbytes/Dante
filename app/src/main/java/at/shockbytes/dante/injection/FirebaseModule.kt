package at.shockbytes.dante.injection

import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides

/**
 * Author:  Martin Macheiner
 * Date:    28.08.2018
 */
@Module
class FirebaseModule {

    @Provides
    fun provideRemoteConfig(): FirebaseRemoteConfig {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.setDefaults(R.xml.remote_config_defaults)
        return remoteConfig
    }
}
package at.shockbytes.dante.injection

import android.content.Context
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.R
import at.shockbytes.dante.storage.FirebaseImageUploadStorage
import at.shockbytes.dante.storage.ImageUploadStorage
import at.shockbytes.tracking.DebugTracker
import at.shockbytes.tracking.FirebaseTracker
import at.shockbytes.tracking.Tracker
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Module
import dagger.Provides
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    28.08.2018
 */
@Module
class FirebaseModule(private val context: Context) {

    @Provides
    fun provideRemoteConfig(): FirebaseRemoteConfig {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .build()
        return FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(configSettings).addOnCompleteListener {
                Timber.d("Firebase Config settings set")
            }
            setDefaultsAsync(R.xml.remote_config_defaults).addOnCompleteListener {
                Timber.d("Firebase defaults set")
            }
        }
    }

    @Provides
    fun provideTracker(): Tracker {
        return if (BuildConfig.DEBUG) {
            DebugTracker()
        } else {
            FirebaseTracker(context)
        }
    }

    @Provides
    fun provideImageUploadStorage(): ImageUploadStorage {
        return FirebaseImageUploadStorage()
    }
}
package at.shockbytes.dante.injection

import android.content.Context
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.R
import at.shockbytes.dante.storage.FirebaseImageUploadStorage
import at.shockbytes.dante.storage.ImageUploadStorage
import at.shockbytes.tracking.DebugTracker
import at.shockbytes.tracking.FirebaseTracker
import at.shockbytes.tracking.Tracker
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
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
            .setMinimumFetchIntervalInSeconds(getFetchInterval())
            .build()
        return FirebaseRemoteConfig.getInstance()
            .apply {
                setConfigSettingsAsync(configSettings).addOnCompleteListener {
                    Timber.d("Firebase Config settings set")
                }
                setDefaultsAsync(R.xml.remote_config_defaults).addOnCompleteListener {
                    Timber.d("Firebase defaults set")
                }
                try {
                    val isActivated = Tasks.await(fetchAndActivate())
                    Timber.d("FirebaseRemoteConfig fetched and activated: $isActivated")
                } catch (exception: Exception) {
                    Timber.e(exception)
                }
            }
    }

    /**
     * If in debug mode, always fetch latest remote config values
     */
    private fun getFetchInterval(): Long {
        return if (BuildConfig.DEBUG) 0 else DEFAULT_MINIMUM_FETCH_INTERVAL_IN_SECONDS
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
    fun provideImageUploadStorage(fbAuth: FirebaseAuth): ImageUploadStorage {
        return FirebaseImageUploadStorage(fbAuth)
    }

    companion object {
        private const val DEFAULT_MINIMUM_FETCH_INTERVAL_IN_SECONDS = 259200L // = 3 days
    }
}
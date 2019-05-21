package at.shockbytes.dante.flagging

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    28.08.2018
 */
class FirebaseFeatureFlagging(private val remoteConfig: FirebaseRemoteConfig) : FeatureFlagging {

    init {

        // Use a fetch interval of 3 days --> This will preserve performance of repetitive fetches
        remoteConfig.fetch(259200L).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Once the config is successfully fetched
                // it must be activated before newly fetched values are returned.
                remoteConfig.activate().addOnCompleteListener { activationTask ->
                    if (activationTask.isSuccessful) {
                        Timber.d("RemoteConfig activation successful!")
                    } else {
                        Timber.d("RemoteConfig activation failed!")
                    }
                }
                Timber.d("RemoteConfig fetch successful!")
            } else {
                Timber.d("RemoteConfig fetch failed!")
            }
        }
    }

    override fun get(flag: FeatureFlag): Boolean {
        return remoteConfig.getBoolean(flag.key)
    }

    override fun updateFlag(key: String, value: Boolean) {
        val msg = "Cannot update feature flags in FirebaseFeatureFlagging implementation!"
        throw UnsupportedOperationException(msg)
    }
}
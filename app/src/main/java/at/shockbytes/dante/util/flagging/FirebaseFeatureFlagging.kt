package at.shockbytes.dante.util.flagging

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
                remoteConfig.activateFetched()
                Timber.d("RemoteConfig fetch successful!")
            } else {
                Timber.d("RemoteConfig fetch failed!")
            }
        }
    }

    override val showSupportersBadge: Boolean
        get() = remoteConfig.getBoolean("supporters_badge")

    override val showBookSuggestions: Boolean
        get() = remoteConfig.getBoolean("book_suggestions")

}
package at.shockbytes.dante.util.flagging

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * @author  Martin Macheiner
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
                Log.d("Dante", "RemoteConfig fetch successful!")
            } else {
                Log.d("Dante", "RemoteConfig fetch failed!")
            }
        }
    }

    override val showSupportersBadge: Boolean
        get() = remoteConfig.getBoolean("supporters_badge")

    override val showBookSuggestions: Boolean
        get() = remoteConfig.getBoolean("book_suggestions")

}
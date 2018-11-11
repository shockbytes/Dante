package at.shockbytes.dante.util.tracking.backend

import android.content.Context
import android.os.Bundle
import at.shockbytes.dante.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * @author  Martin Macheiner
 * Date:    12.06.2018
 */
class FirebaseTrackingBackend(context: Context): TrackingBackend {

    private val fbAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun createTrackEventData(vararg entries: Pair<String, Any>): Map<String, Any> {
        val data = HashMap<String, Any>()
        entries.forEach { (key, value) ->
            data[key] = value
        }
        return data
    }

    override fun trackEvent(event: String, data: Map<String, Any>) {
        // Only track in release version!
        if (!BuildConfig.DEBUG) {
            fbAnalytics.logEvent(event, entries2Bundle(data))
        }
    }

    private fun entries2Bundle(entries: Map<String, Any>): Bundle {
        val data = Bundle()
        entries.forEach { (key, value) ->
            data.putString(key, value.toString())
        }
        return data
    }

}
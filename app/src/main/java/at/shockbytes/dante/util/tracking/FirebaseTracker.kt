package at.shockbytes.dante.util.tracking

import android.content.Context
import android.os.Bundle
import at.shockbytes.dante.util.tracking.event.DanteTrackingEvent
import at.shockbytes.dante.util.tracking.event.TrackingProperty
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class FirebaseTracker(context: Context) : Tracker {

    private val fbAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun trackEvent(event: DanteTrackingEvent) {
        fbAnalytics.logEvent(event.name, createTrackEventData(event.props))
    }

    private fun createTrackEventData(props: List<TrackingProperty>): Bundle {
        val data = Bundle()
        props.forEach { (key, value) ->
            data.putString(key, value.toString())
        }
        return data
    }
}
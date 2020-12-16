package at.shockbytes.tracking

import android.content.Context
import android.os.Bundle
import at.shockbytes.tracking.event.DanteTrackingEvent
import at.shockbytes.tracking.event.TrackingProperty
import at.shockbytes.tracking.properties.BaseProperty
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class FirebaseTracker(context: Context) : Tracker() {

    private val fbAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun trackEvent(event: DanteTrackingEvent) {
        fbAnalytics.logEvent(event.name, createTrackEventData(event.props))
    }

    private fun createTrackEventData(props: List<BaseProperty<Any>>): Bundle {
        val data = Bundle()
        props.forEach { p ->
            data.putString(p.getKey(), p.value.toString())
        }
        return data
    }
}
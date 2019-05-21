package at.shockbytes.dante.tracking

import at.shockbytes.dante.tracking.event.DanteTrackingEvent
import at.shockbytes.dante.tracking.event.TrackingProperty
import timber.log.Timber

class DebugTracker : Tracker {

    override fun trackEvent(event: DanteTrackingEvent) {
        Timber.d("Event: ${event.name} - ${createTrackEventData(event.props)}")
    }

    private fun createTrackEventData(props: List<TrackingProperty>): Map<String, Any> {
        val data = HashMap<String, Any>()
        props.forEach { (key, value) ->
            data[key] = value
        }
        return data
    }
}
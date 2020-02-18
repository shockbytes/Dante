package at.shockbytes.tracking

import at.shockbytes.tracking.event.DanteTrackingEvent
import at.shockbytes.tracking.event.TrackingProperty
import timber.log.Timber

class DebugTracker : Tracker {

    override fun trackEvent(event: DanteTrackingEvent) {
        Timber.d("Event: ${event.name} - ${createTrackEventData(event.props)}")
    }

    private fun createTrackEventData(props: List<TrackingProperty>): Map<String, Any> {
        return props.associateTo(mutableMapOf()) { (key, value) ->
            key to value
        }
    }
}
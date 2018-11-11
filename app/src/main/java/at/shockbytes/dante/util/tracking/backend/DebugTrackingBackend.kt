package at.shockbytes.dante.util.tracking.backend

import timber.log.Timber

class DebugTrackingBackend: TrackingBackend {

    override fun createTrackEventData(vararg entries: Pair<String, Any>): Map<String, Any> {
        val data = HashMap<String, Any>()
        entries.forEach { (key, value) ->
            data[key] = value
        }
        return data
    }

    override fun trackEvent(event: String, data: Map<String, Any>) {
        Timber.d("Event: $event - $data")
    }
}
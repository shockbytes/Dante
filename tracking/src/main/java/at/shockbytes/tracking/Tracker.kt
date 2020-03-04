package at.shockbytes.tracking

import at.shockbytes.tracking.event.DanteTrackingEvent

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
abstract class Tracker {

    var isTrackingAllowed: Boolean = true
        set(value) {
            field = value
            trackEvent(DanteTrackingEvent.TrackingStateChanged(value))
        }

    fun track(event: DanteTrackingEvent) {
        if (isTrackingAllowed) {
            trackEvent(event)
        }
    }

    internal abstract fun trackEvent(event: DanteTrackingEvent)
}
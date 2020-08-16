package at.shockbytes.tracking

import at.shockbytes.tracking.event.DanteTrackingEvent

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
abstract class Tracker {

    var isTrackingAllowed: Boolean? = true
        set(value) {
            // Only track event if value isn't null and there's a previous value (stored in field) too
            if (value != null && field != null && value != field) {
                trackEvent(DanteTrackingEvent.TrackingStateChanged(value))
            }
            field = value
        }

    fun track(event: DanteTrackingEvent) {
        if (isTrackingAllowed == true) {
            trackEvent(event)
        }
    }

    internal abstract fun trackEvent(event: DanteTrackingEvent)
}
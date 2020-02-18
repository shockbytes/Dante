package at.shockbytes.tracking

import at.shockbytes.tracking.event.DanteTrackingEvent
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
abstract class Tracker(initialTrackingPermission: Boolean) {

    var isTrackingAllowed: Boolean = initialTrackingPermission
        set(value) {
            field = value
            Timber.d("Changed tracker to $field")
        }

    fun track(event: DanteTrackingEvent) {
        if (isTrackingAllowed) {
            trackEvent(event)
        }
    }

    abstract fun trackEvent(event: DanteTrackingEvent)
}
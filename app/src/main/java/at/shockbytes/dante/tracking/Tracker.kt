package at.shockbytes.dante.tracking

import at.shockbytes.dante.tracking.event.DanteTrackingEvent

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
interface Tracker {

    fun trackEvent(event: DanteTrackingEvent)
}
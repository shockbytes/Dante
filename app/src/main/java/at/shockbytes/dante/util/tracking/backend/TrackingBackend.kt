package at.shockbytes.dante.util.tracking.backend

/**
 * @author Martin Macheiner
 * Date: 30-Aug-18.
 */
interface TrackingBackend {

    fun createTrackEventData(vararg entries: Pair<String, Any>): Map<String, Any>

    fun trackEvent(event: String, data: Map<String, Any>)

}
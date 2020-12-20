package at.shockbytes.tracking.event

import at.shockbytes.tracking.properties.BaseProperty

data class TrackingProperty(
    private val key: String,
    private val tVal: Any
) : BaseProperty<Any>(tVal) {
    override fun getKey(): String = key
}
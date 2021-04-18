package at.shockbytes.dante.core.flagging

/**
 * Author:  Martin Macheiner
 * Date:    28.08.2018
 */
interface FeatureFlagging {

    operator fun get(flag: FeatureFlag): Boolean

    fun updateFlag(key: String, value: Boolean)
}
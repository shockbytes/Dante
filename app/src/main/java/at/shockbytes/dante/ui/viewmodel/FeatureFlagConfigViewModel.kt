package at.shockbytes.dante.ui.viewmodel

import at.shockbytes.dante.util.flagging.FeatureFlagging
import javax.inject.Inject

class FeatureFlagConfigViewModel @Inject constructor(
    private val featureFlagging: FeatureFlagging
) : BaseViewModel() {
    // TODO Setup feature flags
}
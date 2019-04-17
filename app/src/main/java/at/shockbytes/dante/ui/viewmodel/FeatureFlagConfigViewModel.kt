package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.util.flagging.FeatureFlag
import at.shockbytes.dante.util.flagging.FeatureFlagItem
import at.shockbytes.dante.util.flagging.FeatureFlagging
import javax.inject.Inject

class FeatureFlagConfigViewModel @Inject constructor(
    private val featureFlagging: FeatureFlagging
) : BaseViewModel() {

    private val featureFlags = MutableLiveData<List<FeatureFlagItem>>()

    init {
        loadFeatureFlags()
    }

    fun getFeatureFlagItems(): LiveData<List<FeatureFlagItem>> = featureFlags

    fun updateFeatureFlag(name: String, value: Boolean) {
        featureFlagging.updateFlag(name, value)
    }

    private fun loadFeatureFlags() {
        val items = FeatureFlag.activeFlags().map { f ->
            FeatureFlagItem(f.key, f.displayName, featureFlagging[f])
        }
        featureFlags.postValue(items)
    }
}
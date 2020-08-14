package at.shockbytes.dante.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.util.settings.delegate.SharedPreferencesBoolPropertyDelegate
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import javax.inject.Inject

class OnlineStorageViewModel @Inject constructor(
    sharedPreferences: SharedPreferences,
    private val tracker: Tracker
) : BaseViewModel() {

    enum class InterestedButtonState {
        DEFAULT, INTERESTED
    }

    private val buttonState = MutableLiveData<InterestedButtonState>()
    fun getButtonState(): LiveData<InterestedButtonState> = buttonState

    private var isInterested: Boolean by SharedPreferencesBoolPropertyDelegate(sharedPreferences, ARG_PREFS_INTERESTED_IN_ONLINE, false)

    fun requestButtonState() {

        val state = if (isInterested) {
            InterestedButtonState.INTERESTED
        } else {
            InterestedButtonState.DEFAULT
        }

        buttonState.postValue(state)
    }

    fun userIsInterested() {
        isInterested = true

        trackIsInterested()
        requestButtonState()
    }

    private fun trackIsInterested() {
        tracker.track(DanteTrackingEvent.InterestedInOnlineStorageEvent)
    }

    companion object {

        private const val ARG_PREFS_INTERESTED_IN_ONLINE = "arg_prefs_interested_in_online"
    }
}

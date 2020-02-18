package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.util.settings.LauncherIconState

class LauncherIconPickerViewModel : BaseViewModel() {

    data class LauncherIconItem(
        val iconLauncherIconState: LauncherIconState,
        val isSelected: Boolean
    )

    private val launcherIconItems = MutableLiveData<List<LauncherIconItem>>()
    fun getLauncherItems(): LiveData<List<LauncherIconItem>> = launcherIconItems

    fun requestLauncherItems() {
        val items = LauncherIconState.values().map { state ->
            LauncherIconItem(state, isSelected = false)
        }

        launcherIconItems.postValue(items)
    }
}
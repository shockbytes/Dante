package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.util.settings.LauncherIconState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class LauncherIconPickerViewModel @Inject constructor() : BaseViewModel() {

    data class LauncherIconItem(
        val iconLauncherIconState: LauncherIconState,
        val isSelected: Boolean
    )

    data class ApplyLauncherRequest(
        val activeName: String,
        val disableName: List<String>
    )

    private val launcherIconItems = MutableLiveData<List<LauncherIconItem>>()
    fun getLauncherItems(): LiveData<List<LauncherIconItem>> = launcherIconItems

    private val applyLauncherEvent = PublishSubject.create<ApplyLauncherRequest>()
    fun onApplyLauncherEvent(): Observable<ApplyLauncherRequest> = applyLauncherEvent

    fun requestLauncherItems() {
        val items = LauncherIconState.values().map { state ->
            LauncherIconItem(state, isSelected = false)
        }

        launcherIconItems.postValue(items)
    }

    fun applyLauncher(iconState: LauncherIconState) {

        val activeName = iconState.manifestAliasId

        val disableNames = LauncherIconState.values()
            .filterNot { it.manifestAliasId == activeName }
            .map { it.manifestAliasId }

        applyLauncherEvent.onNext(ApplyLauncherRequest(activeName, disableNames))
    }
}
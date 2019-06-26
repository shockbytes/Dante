package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.announcement.Announcement
import at.shockbytes.dante.announcement.AnnouncementProvider
import javax.inject.Inject

class AnnouncementViewModel @Inject constructor(
    private val announcementProvider: AnnouncementProvider
) : BaseViewModel() {

    private val announcementState = MutableLiveData<AnnouncementState>()
    fun getAnnouncementState(): LiveData<AnnouncementState> = announcementState

    fun requestAnnouncementState() {
        val state = announcementProvider.getActiveAnnouncement()?.let { announcement ->
            AnnouncementState.Active(announcement)
        } ?: AnnouncementState.Inactive

        announcementState.postValue(state)
    }

    fun markAnnouncementAsSeen(announcement: Announcement) {
        announcementProvider.markAnnouncementAsSeen(announcement)
    }

    sealed class AnnouncementState {

        data class Active(val announcement: Announcement) : AnnouncementState()

        object Inactive : AnnouncementState()
    }
}
package at.shockbytes.dante.announcement

import android.content.SharedPreferences

class SharedPrefsAnnouncementProvider(
    private val sharedPreferences: SharedPreferences
) : AnnouncementProvider {

    private val activeAnnouncement: Announcement? = null

    override fun getActiveAnnouncement(): Announcement? {

        val isSeen = isActiveAnnouncementSeen()
        return if (!isSeen) {
            activeAnnouncement
        } else {
            null
        }
    }

    private fun isActiveAnnouncementSeen(): Boolean {
        return sharedPreferences.getBoolean(activeAnnouncement?.key, false)
    }

    override fun markAnnouncementAsSeen(announcement: Announcement) {
        sharedPreferences.edit()
            .putBoolean(announcement.key, true)
            .apply()
    }
}
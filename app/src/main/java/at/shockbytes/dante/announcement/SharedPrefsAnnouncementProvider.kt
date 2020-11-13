package at.shockbytes.dante.announcement

import android.content.SharedPreferences
import at.shockbytes.dante.R
import at.shockbytes.dante.navigation.Destination

class SharedPrefsAnnouncementProvider(
    private val sharedPreferences: SharedPreferences
) : AnnouncementProvider {

    private val activeAnnouncement: Announcement? = Announcement(
        key = "fixed_backup_announcement",
        titleRes = R.string.announcement_fixed_backup_title,
        descriptionRes = R.string.announcement_fixed_backup_description,
        illustration = Announcement.Illustration.ImageIllustration(R.drawable.ic_google_drive),
        action = Announcement.Action.OpenScreen(destination = Destination.BookStorage)
    )

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
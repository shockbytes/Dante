package at.shockbytes.dante.announcement

import android.content.SharedPreferences
import at.shockbytes.dante.R

class SharedPrefsAnnouncementProvider(
    private val sharedPreferences: SharedPreferences
) : AnnouncementProvider {

    private val activeAnnouncement = Announcement(
        key = "open_source_announcement",
        titleRes = R.string.announcement_open_source_title,
        descriptionRes = R.string.announcement_open_source_description,
        illustration = Announcement.Illustration.LottieIllustration(R.raw.lottie_github)
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
        return sharedPreferences.getBoolean(activeAnnouncement.key, false)
    }

    override fun markAnnouncementAsSeen(announcement: Announcement) {
        sharedPreferences.edit()
            .putBoolean(announcement.key, true)
            .apply()
    }
}
package at.shockbytes.dante.announcement

import android.content.SharedPreferences
import at.shockbytes.dante.R

class SharedPrefsAnnouncementProvider(
    private val sharedPreferences: SharedPreferences
) : AnnouncementProvider {

    private val activeAnnouncement: Announcement = Announcement(
        key = "danteX",
        titleRes = R.string.announcement_suggestion_title,
        descriptionRes = R.string.announcement_suggestion_description,
        illustration = Announcement.Illustration.LottieIllustration(R.raw.in_development),
        action = Announcement.Action.OpenUrl(
            actionLabel = R.string.open_github,
            url = "https://github.com/shockbytes/DanteX"
        )
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
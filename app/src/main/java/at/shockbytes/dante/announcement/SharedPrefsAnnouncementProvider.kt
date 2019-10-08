package at.shockbytes.dante.announcement

import android.content.SharedPreferences
import at.shockbytes.dante.R

class SharedPrefsAnnouncementProvider(
    private val sharedPreferences: SharedPreferences
) : AnnouncementProvider {

    private val activeAnnouncement = Announcement(
        key = "translation_announcement",
        titleRes = R.string.announcement_translation_title,
        descriptionRes = R.string.announcement_translation_description,
        illustration = Announcement.Illustration.LottieIllustration(R.raw.lottie_translation),
        action = Announcement.Action.Mail(R.string.mail_subject_translation)
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
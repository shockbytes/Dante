package at.shockbytes.dante.announcement

interface AnnouncementProvider {

    fun getActiveAnnouncement(): Announcement?

    fun markAnnouncementAsSeen(announcement: Announcement)
}
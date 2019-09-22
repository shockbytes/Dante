package at.shockbytes.dante.core.network

object DownloadUtils {

    private const val ZOOM_LINK = "&zoom="

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun increaseGoogleThumbnailResolutionLink(gLink: String, zoomFactor: Int): String {

        if (zoomFactor !in 1..9) {
            throw IllegalArgumentException("Zoom factor must be in range 0..9 (factor=$zoomFactor)")
        }

        val idx = gLink.lastIndexOf(ZOOM_LINK) + ZOOM_LINK.length
        if (idx < 0) {
            throw IllegalStateException("Cannot find $ZOOM_LINK in $gLink")
        }

        return gLink.toCharArray().apply {
            this[idx] = zoomFactor.toString()[0]
        }.joinToString("")
    }
}
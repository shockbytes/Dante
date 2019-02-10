package at.shockbytes.dante.network

import org.junit.Assert
import org.junit.Assert.fail
import org.junit.Test

class DownloadUtilsTest {

    @Test
    fun `test Google thumbnail increase factor by 2`() {
        val input = "http://books.google.com/books/content?id=69bYyY9xiKYC&printsec=frontcover&img=2&zoom=1&edge=curl&source=gbs_api"
        val zoomFactor = 2
        val expected = "http://books.google.com/books/content?id=69bYyY9xiKYC&printsec=frontcover&img=2&zoom=2&edge=curl&source=gbs_api"

        Assert.assertEquals(expected, DownloadUtils.increaseGoogleThumbnailResolutionLink(input, zoomFactor))
    }

    @Test
    fun `test Google thumbnail increase factor by 6`() {
        val input = "http://books.google.com/books/content?id=69bYyY9xiKYC&printsec=frontcover&img=2&zoom=1&edge=curl&source=gbs_api"
        val zoomFactor = 6
        val expected = "http://books.google.com/books/content?id=69bYyY9xiKYC&printsec=frontcover&img=2&zoom=6&edge=curl&source=gbs_api"

        Assert.assertEquals(expected, DownloadUtils.increaseGoogleThumbnailResolutionLink(input, zoomFactor))
    }

    @Test
    fun `test Google thumbnail increase with invalid zoom factor 0`() {
        val input = "http://books.google.com/books/content?id=69bYyY9xiKYC&printsec=frontcover&img=2&zoom=1&edge=curl&source=gbs_api"
        val zoomFactor = 0

        try {
            DownloadUtils.increaseGoogleThumbnailResolutionLink(input, zoomFactor)
            fail()
        } catch (e: Exception) {
            Assert.assertEquals(e.javaClass.name, IllegalArgumentException::class.qualifiedName)
        }
    }

    @Test
    fun `test Google thumbnail increase with invalid zoom factor 10`() {
        val input = "http://books.google.com/books/content?id=69bYyY9xiKYC&printsec=frontcover&img=2&zoom=1&edge=curl&source=gbs_api"
        val zoomFactor = 10

        try {

            DownloadUtils.increaseGoogleThumbnailResolutionLink(input, zoomFactor)
            fail()
        } catch (e: Exception) {
            Assert.assertEquals(e.javaClass.name, IllegalArgumentException::class.qualifiedName)
        }
    }
}
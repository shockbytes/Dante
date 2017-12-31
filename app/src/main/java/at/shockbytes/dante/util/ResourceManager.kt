package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import at.shockbytes.dante.R
import at.shockbytes.dante.util.books.Book
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */

object ResourceManager {

    fun formatTimestamp(timeMillis: Long): String {
        return SimpleDateFormat("dd. MMM yyy - kk:mm", Locale.getDefault())
                .format(Date(timeMillis))
    }

    fun createSharingIntent(c: Context, b: Book): Intent {

        val msg = c.getString(R.string.share_template, b.title, b.googleBooksLink)
        return Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, msg)
                .setType("text/plain")
    }

}

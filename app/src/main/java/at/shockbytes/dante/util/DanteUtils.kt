package at.shockbytes.dante.util

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.appcompat.widget.PopupMenu
import at.shockbytes.dante.R
import at.shockbytes.dante.util.books.Book
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
 */

object DanteUtils {

    const val rcAddBook = 0x2512
    const val extraBookId = "extra_book_downloaded"
    const val maxFetchAmount = 10

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

    fun tintImage(context: Context, @DrawableRes image: Int, @ColorInt tintColor: Int): Bitmap {

        val bm = BitmapFactory.decodeResource(context.resources, image)

        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        val bitmapResult = Bitmap.createBitmap(bm.width, bm.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapResult)
        canvas.drawBitmap(bm, 0f, 0f, paint)
        return bitmapResult
    }

    fun listPopAnimation(animationList: List<View>, duration: Long = 300, initialDelay: Long = 300,
                         interpolator: Interpolator = OvershootInterpolator(2f)) {

        animationList.forEach {
            it.alpha = 0f; it.scaleX = 0.3f; it.scaleY = 0.3f
        }

        animationList.forEachIndexed { index, view ->
            view.animate().scaleY(1f).scaleX(1f).alpha(1f)
                    .setInterpolator(interpolator)
                    .setStartDelay((initialDelay + (index * 100L)))
                    .setDuration(duration)
                    .withEndAction { view.alpha = 1f; view.scaleX = 1f; view.scaleY = 1f } // <-- If anim failed, set it in the end
                    .start()
        }
    }

    fun isPortrait(context: Context?): Boolean {
        return context?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    fun vector2Drawable(c: Context, res: Int) = AppCompatDrawableManager.get().getDrawable(c, res)

    fun tryShowIconsInPopupMenu(menu: PopupMenu) {

        try {
            val fieldPopup = menu.javaClass.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val popup = fieldPopup.get(menu) as MenuPopupHelper
            popup.setForceShowIcon(true)
        } catch (e: Exception) {
            Log.d("Dante", "Cannot force to show icons in popupmenu")
        }
    }

    fun indexForNavigationItemId(itemId: Int): Int? {
        return when (itemId) {
            R.id.menu_navigation_upcoming -> 0
            R.id.menu_navigation_current -> 1
            R.id.menu_navigation_done -> 2
        // TODO Enable later R.id.menu_navigation_suggestions -> 3
            else -> null
        }
    }

}

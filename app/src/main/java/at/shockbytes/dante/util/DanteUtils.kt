package at.shockbytes.dante.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.AppCompatDrawableManager
import android.support.v7.widget.PopupMenu
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author  Martin Macheiner
 * Date:    30.04.2017.
 */

object DanteUtils {

    const val rcSignIn = 0x8944

    fun formatTimestamp(timeMillis: Long): String {
        return SimpleDateFormat("dd. MMM yyy - kk:mm", Locale.getDefault())
                .format(Date(timeMillis))
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

    fun vector2Drawable(c: Context, res: Int): Drawable = AppCompatDrawableManager.get().getDrawable(c, res)

    fun tryShowIconsInPopupMenu(menu: PopupMenu) {

        try {
            val fieldPopup = menu.javaClass.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val popup = fieldPopup.get(menu) as MenuPopupHelper
            popup.setForceShowIcon(true)
        } catch (e: Exception) {
            Timber.e("Cannot force to show icons in popupmenu")
        }
    }

    fun isNetworkAvailable(ctx: Context): Boolean {
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo?.isConnected ?: false
    }


}

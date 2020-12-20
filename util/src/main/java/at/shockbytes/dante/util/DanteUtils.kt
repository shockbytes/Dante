package at.shockbytes.dante.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.annotation.AnimRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import at.shockbytes.util.AppUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Author:  Martin Macheiner
 * Date:    30.04.2017
 */
object DanteUtils {

    const val RC_SIGN_IN = 0x8944

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

    fun applyCustomFontToText(text: String, fontName: String = "Montserrat"): CharSequence {
        return SpannableStringBuilder(text).apply {
            val font = Typeface.create(fontName, Typeface.NORMAL)
            this.setSpan(FontSpan(font), 4, this.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
    }

    fun addFragmentToActivity(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        frameId: Int,
        addToBackStack: Boolean,
        @AnimRes inAnim: Int = R.anim.fade_in,
        @AnimRes outAnim: Int = R.anim.fade_out
    ) {
        val transaction = fragmentManager.beginTransaction()
        transaction.setCustomAnimations(inAnim, outAnim, inAnim, outAnim)
        if (addToBackStack) transaction.addToBackStack(fragment.javaClass.name)
        transaction.add(frameId, fragment)
        transaction.commit()
    }

    fun computePercentage(x: Double, total: Double): Int {
        return if (total > 0) {
            ((x / total) * 100).roundToInt()
        } else 0
    }

    fun String.checkUrlForHttps(): String {
        return if (startsWith("http://")) {
            replace("http://", "https://")
        } else this
    }

    fun vector2Drawable(c: Context, res: Int): Drawable = AppCompatDrawableManager.get().getDrawable(c, res)

    fun isNetworkAvailable(ctx: Context): Boolean {
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo?.isConnected ?: false
    }

    fun buildTimestampFromDate(year: Int, month: Int, day: Int): Long {

        val cal = Calendar.getInstance()

        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.timeInMillis
    }

    @SuppressLint("RestrictedApi")
    fun getBitmap(context: Context, drawableId: Int): Bitmap {

        val drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId)

        // Handle special case if drawable is vector drawable, which is only supported in API level 21
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && (drawable is VectorDrawable)) {
            getBitmap(drawable, AppUtils.convertDpInPixel(24, context))
        } else {
            when (drawable) {
                is BitmapDrawable -> BitmapFactory.decodeResource(context.resources, drawableId)
                is VectorDrawableCompat -> getBitmap(drawable, AppUtils.convertDpInPixel(24, context))
                else -> throw IllegalArgumentException("Unsupported drawable type")
            }
        }
    }

    private fun getBitmap(vectorDrawable: VectorDrawableCompat, padding: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(padding, padding, canvas.width - padding, canvas.height - padding)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    private fun getBitmap(vectorDrawable: VectorDrawable, padding: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(padding, padding, canvas.width - padding, canvas.height - padding)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    fun createRoundedBitmapFromColor(
        context: Context,
        size: Int,
        @ColorInt color: Int
    ): RoundedBitmapDrawable {

        val image = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        image.eraseColor(color)

        // val c = Canvas(image)
        // c.drawBitmap(image, 0f, 0f, null)

        val rdb = RoundedBitmapDrawableFactory.create(context.resources, image)
        rdb.isCircular = true
        return rdb
    }
}

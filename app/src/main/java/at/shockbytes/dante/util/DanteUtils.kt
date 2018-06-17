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
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.book.BookStatistics
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*




/**
 * @author Martin Macheiner
 * Date: 30.04.2017.
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
            Log.d("Dante", "Cannot force to show icons in popupmenu")
        }
    }

    fun isNetworkAvailable(ctx: Context): Boolean {
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo?.isConnectedOrConnecting ?: false
    }

    fun buildStatistics(books: List<BookEntity>): Single<BookStatistics> {
        return Single.fromCallable {

            val upcoming = books.filter { it.state == BookState.READ_LATER }
            val done = books.filter { it.state == BookState.READ }
            val reading = books.filter { it.state == BookState.READING }

            // Add pages in the currently read book to read pages
            val pagesRead = done.sumBy { it.pageCount } + reading.sumBy { it.currentPage }
            // Add pages waiting in the current book to waiting pages
            val pagesWaiting = upcoming.sumBy { it.pageCount } + reading.sumBy { it.pageCount - it.currentPage }
            val (fastestBook, slowestBook) = BookStatistics.bookDurations(done)
            val avgBooksPerMonth = BookStatistics.averageBooksPerMonth(done)
            val mostReadingMonth = BookStatistics.mostReadingMonth(done)

            BookStatistics(pagesRead, pagesWaiting,
                    done.size, upcoming.size,
                    fastestBook, slowestBook,
                    avgBooksPerMonth, mostReadingMonth)
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
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

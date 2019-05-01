package at.shockbytes.dante.ui.widget

import android.content.Context
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.data.BookEntityDao
import io.reactivex.disposables.CompositeDisposable
import at.shockbytes.dante.ui.image.GlideImageLoader.loadBitmap
import at.shockbytes.dante.util.DanteUtils.checkUrlForHttps
import timber.log.Timber
import at.shockbytes.dante.util.DanteUtils

class DanteRemoteViewsFactory(
    private val context: Context,
    private val bookEntityDao: BookEntityDao
) : RemoteViewsService.RemoteViewsFactory {

    private val compositeDisposable = CompositeDisposable()

    private var currentBooks = listOf<BookEntity>()

    override fun onCreate() {
    }

    override fun getLoadingView(): RemoteViews? {
        // TODO Implement later, return null for now
        return null
    }

    override fun getItemId(position: Int): Long = currentBooks[position].id

    override fun onDataSetChanged() {
        currentBooks = ArrayList(bookEntityDao.booksCurrentlyReading)

        Timber.d("onDataSetChanged() - New book size: ${currentBooks.size}")
    }

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {

        val book = currentBooks[position]

        return RemoteViews(context.packageName, R.layout.item_app_widget).apply {

            book.thumbnailAddress?.let { url ->
                val bm = Uri.parse(checkUrlForHttps(url)).loadBitmap(context).blockingGet()
                setImageViewBitmap(R.id.item_app_widget_icon, bm)
            }
            setTextViewText(R.id.item_app_widget_title, book.title)

            val pages = context.getString(R.string.detail_pages, book.currentPage, book.pageCount)
            setTextViewText(R.id.item_app_widget_tv_pages, pages)

            setTextViewText(R.id.item_app_widget_tv_author, book.author)

            val progress = DanteUtils.computePercentage(
                book.currentPage.toDouble(),
                book.pageCount.toDouble()
            )
            setTextViewText(R.id.item_app_widget_tv_progress, context.getString(R.string.percentage_formatter, progress))
        }
    }

    override fun getCount(): Int = currentBooks.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        compositeDisposable.clear()
    }

    companion object {
        const val ACTION_DECREASE = "ACTION_PAGES_DEC"
        const val ACTION_INCREASE = "ACTION_PAGES_INC"
    }
}
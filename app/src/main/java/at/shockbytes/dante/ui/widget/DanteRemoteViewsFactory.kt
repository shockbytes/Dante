package at.shockbytes.dante.ui.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import io.reactivex.disposables.CompositeDisposable
import at.shockbytes.dante.core.image.GlideImageLoader.loadBitmap
import at.shockbytes.dante.util.DanteUtils.checkUrlForHttps
import timber.log.Timber
import at.shockbytes.dante.util.DanteUtils
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.sort.SortComparators
import at.shockbytes.dante.util.toBitmap

class DanteRemoteViewsFactory(
    private val context: Context,
    private val bookRepository: BookRepository,
    private val danteSettings: DanteSettings
) : RemoteViewsService.RemoteViewsFactory {

    private val compositeDisposable = CompositeDisposable()

    private var currentBooks = listOf<BookEntity>()

    override fun onCreate() {
        Timber.d("appwidget - remoteviews factory onCreate()")
    }

    override fun getLoadingView(): RemoteViews? {
        // TODO Implement later, return null for now
        return null
    }

    override fun getItemId(position: Int): Long = currentBooks[position].id

    override fun onDataSetChanged() {

        val sorter = SortComparators.of(danteSettings.sortStrategy)
        currentBooks = ArrayList(bookRepository.booksCurrentlyReading.sortedWith(sorter))

        Timber.d("appwidget - remoteviews factory - onDataSetChanged() - books: ${currentBooks.size}")
    }

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {

        val book = currentBooks[position]

        return RemoteViews(context.packageName, R.layout.item_app_widget).apply {

            val thumbnailBitmap = getThumbnailBitmap(book.thumbnailAddress)
            setImageViewBitmap(R.id.item_app_widget_icon, thumbnailBitmap)

            setTextViewText(R.id.item_app_widget_title, book.title)

            val pages = context.getString(R.string.detail_pages, book.currentPage, book.pageCount)
            setTextViewText(R.id.item_app_widget_tv_pages, pages)

            setTextViewText(R.id.item_app_widget_tv_author, book.author)

            setOnClickFillInIntent(R.id.item_app_widget_layout, createFillInIntent(book))

            val progress = DanteUtils.computePercentage(
                book.currentPage.toDouble(),
                book.pageCount.toDouble()
            )
            setTextViewText(R.id.item_app_widget_tv_progress, context.getString(R.string.percentage_formatter, progress))
        }
    }

    private fun getThumbnailBitmap(thumbnailAddress: String?): Bitmap? {
        Timber.d("Thumbnail: $thumbnailAddress")
        return if (!thumbnailAddress.isNullOrEmpty()) {
            try {
                thumbnailAddress.checkUrlForHttps().toUri().loadBitmap(context).blockingGet()
            } catch (e: Exception) {
                e.printStackTrace()
                ContextCompat.getDrawable(context, R.drawable.ic_placeholder)?.toBitmap()
            }
        } else {
            ContextCompat.getDrawable(context, R.drawable.ic_placeholder)?.toBitmap()
        }
    }

    override fun getCount(): Int = currentBooks.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        compositeDisposable.clear()
    }

    private fun createFillInIntent(book: BookEntity): Intent {
        return Intent()
            .putExtra(DanteAppWidget.EXTRA_BOOK_ID, book.id)
            .putExtra(DanteAppWidget.EXTRA_BOOK_TITLE, book.title)
    }
}
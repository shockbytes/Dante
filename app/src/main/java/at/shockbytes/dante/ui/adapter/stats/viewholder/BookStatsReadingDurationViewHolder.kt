package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_reading_duration.*

class BookStatsReadingDurationViewHolder(
    override val containerView: View,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<BookStatsItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsItem, position: Int) {
        with(content as BookStatsItem.ReadingDuration) {

            when (this) {
                BookStatsItem.ReadingDuration.Empty -> {
                    showEmptyState()
                }
                is BookStatsItem.ReadingDuration.Present -> {
                    showReadingDuration(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        item_stats_reading_duration_empty.setVisible(true)
        item_stats_reading_duration_content.setVisible(false)
    }

    private fun showReadingDuration(content: BookStatsItem.ReadingDuration.Present) {
        item_stats_reading_duration_empty.setVisible(false)
        item_stats_reading_duration_content.setVisible(true)

        with(content) {
            bare_bone_book_view_slowest_book.apply {
                setTitle(slowest.book.title)

                val slowestUrl = slowest.book.thumbnailAddress
                if (slowestUrl != null) {
                    imageLoader.loadImageWithCornerRadius(
                        containerView.context,
                        slowestUrl,
                        imageView,
                        cornerDimension = containerView.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }
            }
            val slowestDays = containerView.context.resources.getQuantityString(R.plurals.days, slowest.days, slowest.days)
            tv_item_stats_reading_duration_slowest_duration.text = slowestDays

            bare_bone_book_view_fastest_book.apply {
                setTitle(fastest.book.title)

                val fastestUrl = fastest.book.thumbnailAddress
                if (fastestUrl != null) {
                    imageLoader.loadImageWithCornerRadius(
                        containerView.context,
                        fastestUrl,
                        imageView,
                        cornerDimension = containerView.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }
            }
            val fastestDays = containerView.context.resources.getQuantityString(R.plurals.days, fastest.days, fastest.days)
            tv_item_stats_reading_duration_fastest_duration.text = fastestDays
        }
    }
}
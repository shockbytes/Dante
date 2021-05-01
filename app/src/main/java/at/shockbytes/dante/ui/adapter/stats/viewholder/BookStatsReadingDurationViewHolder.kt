package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemStatsReadingDurationBinding
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class BookStatsReadingDurationViewHolder(
    override val containerView: View,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<BookStatsViewItem>(containerView), LayoutContainer {

    private val vb = ItemStatsReadingDurationBinding.bind(containerView)

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.ReadingDuration) {

            when (this) {
                BookStatsViewItem.ReadingDuration.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.ReadingDuration.Present -> {
                    showReadingDuration(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsReadingDurationEmpty.root.setVisible(true)
        vb.itemStatsReadingDurationContent.setVisible(false)
    }

    private fun showReadingDuration(content: BookStatsViewItem.ReadingDuration.Present) {
        vb.itemStatsReadingDurationEmpty.root.setVisible(false)
        vb.itemStatsReadingDurationContent.setVisible(true)

        with(content) {
            vb.bareBoneBookViewSlowestBook.apply {
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
            vb.tvItemStatsReadingDurationSlowestDuration.text = slowestDays

            vb.bareBoneBookViewFastestBook.apply {
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
            vb.tvItemStatsReadingDurationFastestDuration.text = fastestDays
        }
    }
}
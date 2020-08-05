package at.shockbytes.dante.ui.adapter.main

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.util.ColorUtils.desaturateAndDevalue
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.isNightModeEnabled
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import com.google.android.material.chip.Chip
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_book.*

class BookViewHolder(
        override val containerView: View,
        private val imageLoader: ImageLoader,
        private val onOverflowActionClickedListener: (BookEntity) -> Unit,
        private val onLabelClickedListener: (BookLabel) -> Unit
) : BaseAdapter.ViewHolder<BookAdapterEntity>(containerView), LayoutContainer {

    private fun context(): Context = containerView.context

    override fun bindToView(content: BookAdapterEntity, position: Int) {
        with(content as BookAdapterEntity.Book) {
            updateTexts(bookEntity)
            updateImageThumbnail(bookEntity.thumbnailAddress)
            updateProgress(bookEntity)
            updateLabels(bookEntity.labels)
            setOverflowClickListener(bookEntity)
        }
    }

    private fun updateLabels(labels: List<BookLabel>) {
        chips_item_book_label.removeAllViews()

        val isNightModeEnabled = context().isNightModeEnabled()

        labels
                .map { label ->
                    buildChipViewFromLabel(label, isNightModeEnabled)
                }
                .forEach(chips_item_book_label::addView)
    }

    private fun buildChipViewFromLabel(label: BookLabel, isNightModeEnabled: Boolean): Chip {

        val chipColor = if (isNightModeEnabled) {
            desaturateAndDevalue(Color.parseColor(label.hexColor), by = 0.25f)
        } else {
            Color.parseColor(label.hexColor)
        }

        return Chip(containerView.context).apply {
            chipBackgroundColor = ColorStateList.valueOf(chipColor)
            text = label.title
            setTextColor(Color.WHITE)
            setOnClickListener {
                onLabelClickedListener(label)
            }
        }
    }

    private fun setOverflowClickListener(content: BookEntity) {
        item_book_img_overflow.setOnClickListener {
            onOverflowActionClickedListener(content)
        }
    }

    private fun updateProgress(t: BookEntity) {

        val showProgress = t.reading && t.hasPages

        if (showProgress) {
            val progress = DanteUtils.computePercentage(
                    t.currentPage.toDouble(),
                    t.pageCount.toDouble()
            )
            animateBookProgress(progress)
            item_book_tv_progress.text = context().getString(R.string.percentage_formatter, progress)
        }

        item_book_group_progress.setVisible(showProgress)
    }

    private fun animateBookProgress(progress: Int) {
        item_book_pb.progress = progress
    }

    private fun updateImageThumbnail(address: String?) {
        if (!address.isNullOrEmpty()) {
            imageLoader.loadImageWithCornerRadius(
                    context(),
                    address,
                    item_book_img_thumb,
                    cornerDimension = context().resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        } else {
            // Books with no image will recycle another cover if not cleared here
            item_book_img_thumb.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun updateTexts(t: BookEntity) {
        item_book_txt_title.text = t.title
        item_book_txt_author.text = t.author
        item_book_txt_subtitle.apply {
            text = t.subTitle
            setVisible(t.subTitle.isNotEmpty())
        }
    }

    companion object {

        fun fromView(
                view: View,
                imageLoader: ImageLoader,
                onOverflowActionClickedListener: (BookEntity) -> Unit,
                onLabelClickedListener: (BookLabel) -> Unit
        ): BookViewHolder {
            return BookViewHolder(
                    view,
                    imageLoader,
                    onOverflowActionClickedListener,
                    onLabelClickedListener
            )
        }
    }
}
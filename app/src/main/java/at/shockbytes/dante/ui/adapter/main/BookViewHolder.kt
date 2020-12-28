package at.shockbytes.dante.ui.adapter.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.ui.view.ChipFactory
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_book.*

class BookViewHolder(
    override val containerView: View,
    private val imageLoader: ImageLoader,
    private val onOverflowActionClickedListener: ((BookEntity) -> Unit)?,
    private val onLabelClickedListener: ((BookLabel) -> Unit)?
) : BaseAdapter.ViewHolder<BookAdapterItem>(containerView), LayoutContainer {

    private fun context(): Context = containerView.context

    override fun bindToView(content: BookAdapterItem, position: Int) {
        with(content as BookAdapterItem.Book) {
            updateTexts(bookEntity)
            updateImageThumbnail(bookEntity.thumbnailAddress)
            updateProgress(bookEntity)
            updateLabels(bookEntity.labels)
            setOverflowClickListener(bookEntity)
        }
    }

    private fun updateLabels(labels: List<BookLabel>) {
        chips_item_book_label.apply {
            setVisible(labels.isNotEmpty())
            removeAllViews()
        }

        labels
            .map { label ->
                ChipFactory.buildChipViewFromLabel(
                    context(),
                    label,
                    onLabelClickedListener
                )
            }
            .forEach(chips_item_book_label::addView)
    }

    private fun setOverflowClickListener(content: BookEntity) {
        item_book_img_overflow.setOnClickListener {
            onOverflowActionClickedListener?.invoke(content)
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

        fun forParent(
            parent: ViewGroup,
            imageLoader: ImageLoader,
            onOverflowActionClickedListener: ((BookEntity) -> Unit)?,
            onLabelClickedListener: ((BookLabel) -> Unit)?
        ): BookViewHolder {
            return BookViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false),
                imageLoader,
                onOverflowActionClickedListener,
                onLabelClickedListener
            )
        }
    }
}
package at.shockbytes.dante.ui.adapter.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemBookBinding
import at.shockbytes.dante.ui.view.ChipFactory
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class BookViewHolder(
    override val containerView: View,
    private val imageLoader: ImageLoader,
    private val onOverflowActionClickedListener: ((BookEntity) -> Unit)?,
    private val onLabelClickedListener: ((BookLabel) -> Unit)?
) : BaseAdapter.ViewHolder<BookAdapterItem>(containerView), LayoutContainer {

    private val vb = ItemBookBinding.bind(containerView)

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
        vb.chipsItemBookLabel.apply {
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
            .forEach(vb.chipsItemBookLabel::addView)
    }

    private fun setOverflowClickListener(content: BookEntity) {
        vb.itemBookImgOverflow.setOnClickListener {
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
            vb.itemBookTvProgress.text = context().getString(R.string.percentage_formatter, progress)
        }

        vb.itemBookGroupProgress.setVisible(showProgress)
    }

    private fun animateBookProgress(progress: Int) {
        vb.itemBookPb.progress = progress
    }

    private fun updateImageThumbnail(address: String?) {
        if (!address.isNullOrEmpty()) {
            imageLoader.loadImageWithCornerRadius(
                context(),
                address,
                vb.itemBookImgThumb,
                cornerDimension = context().resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        } else {
            // Books with no image will recycle another cover if not cleared here
            vb.itemBookImgThumb.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun updateTexts(t: BookEntity) {
        vb.itemBookTxtTitle.text = t.title
        vb.itemBookTxtAuthor.text = t.author
        vb.itemBookTxtSubtitle.apply {
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
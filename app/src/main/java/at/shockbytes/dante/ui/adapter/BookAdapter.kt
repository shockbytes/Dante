package at.shockbytes.dante.ui.adapter

import android.os.Build
import android.transition.TransitionManager
import android.view.HapticFeedbackConstants
import androidx.recyclerview.widget.DiffUtil
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.view.BookDiffUtilCallback
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_book.*
import java.util.Collections

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 */
class BookAdapter(
    private val recyclerView: RecyclerView,
    private val imageLoader: ImageLoader,
    private val onActionClickedListener: OnBookActionClickedListener
) : BaseAdapter<BookEntity>(recyclerView.context), ItemTouchHelperAdapter {

    private var expandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter<BookEntity>.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_book, parent, false))
    }

    override fun onItemDismiss(position: Int) {
        val removed = data.removeAt(position)
        onItemMoveListener?.onItemDismissed(removed, position)
    }

    override fun onItemMove(from: Int, to: Int): Boolean {

        // Switch the item within the collection
        if (from < to) {
            for (i in from until to) {
                Collections.swap(data, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(data, i, i - 1)
            }
        }
        notifyItemMoved(from, to)
        onItemMoveListener?.onItemMove(data[from], from, to)

        return true
    }

    override fun onItemMoveFinished() {
        onItemMoveListener?.onItemMoveFinished()
    }

    fun updateData(books: List<BookEntity>) {
        val diffResult = DiffUtil.calculateDiff(BookDiffUtilCallback(data, books))

        data.clear()
        data.addAll(books)

        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(
        override val containerView: View
    ) : BaseAdapter<BookEntity>.ViewHolder(containerView), LayoutContainer {

        override fun bindToView(t: BookEntity) {
            updateTexts(t)
            updateImageThumbnail(t.thumbnailAddress)
            updateProgress(t)
            setActionButtons(t)
        }

        private fun setActionButtons(item: BookEntity) {
            setActionButtonListener(item)
            hideObsoleteActionButton(item.state)
            setActionButtonAnimations(item)
        }

        private fun setActionButtonListener(item: BookEntity) {
            item_book_actions_btn_move_to_upcoming.setOnClickListener { v ->
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onActionClickedListener.onMoveToUpcoming(item)
                deleteEntity(item)
                expandedPosition = -1
            }
            item_book_actions_btn_move_to_reading.setOnClickListener { v ->
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onActionClickedListener.onMoveToCurrent(item)
                deleteEntity(item)
                expandedPosition = -1
            }
            item_book_actions_btn_move_to_read.setOnClickListener { v ->
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onActionClickedListener.onMoveToDone(item)
                deleteEntity(item)
                expandedPosition = -1
            }
            item_book_actions_btn_share.setOnClickListener { v ->
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onActionClickedListener.onShare(item)
            }
            item_book_actions_btn_delete.setOnClickListener { v ->
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onActionClickedListener.onDelete(item) { onDeletionConfirmed ->
                    if (onDeletionConfirmed) {
                        deleteEntity(item)
                        expandedPosition = -1
                    }
                }
            }
        }

        private fun hideObsoleteActionButton(state: BookState) {
            val actionButton = when (state) {
                BookState.READ_LATER -> item_book_actions_btn_move_to_upcoming
                BookState.READING -> item_book_actions_btn_move_to_reading
                BookState.READ -> item_book_actions_btn_move_to_read
            }
            actionButton.setVisible(false)
        }

        private fun setActionButtonAnimations(item: BookEntity) {
            val position = getLocation(item)
            val isExpanded = position == expandedPosition

            item_book_container_actions.setVisible(isExpanded)
            containerView.isActivated = isExpanded
            item_book_img_overflow.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                TransitionManager.beginDelayedTransition(recyclerView)
                notifyDataSetChanged()
            }
        }

        private fun updateProgress(t: BookEntity) {

            val showProgress = t.reading && t.hasPages

            if (showProgress) {
                val progress = DanteUtils.computePercentage(
                        t.currentPage.toDouble(),
                        t.pageCount.toDouble()
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    item_book_pb.setProgress(progress, true)
                } else {
                    item_book_pb.progress = progress
                }
                item_book_tv_progress.text = context.getString(R.string.percentage_formatter, progress)
            }

            item_book_group_progress.setVisible(showProgress)
        }

        private fun updateImageThumbnail(address: String?) {

            if (!address.isNullOrEmpty()) {
                imageLoader.loadImageWithCornerRadius(
                    context,
                    address,
                    item_book_img_thumb,
                    cornerDimension = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                )
            } else {
                // Books with no image will recycle another cover if not cleared here
                item_book_img_thumb.setImageResource(R.drawable.ic_placeholder)
            }
        }

        private fun updateTexts(t: BookEntity) {
            item_book_txt_title.text = t.title
            item_book_txt_author.text = t.author
            item_book_txt_subtitle.text = t.subTitle
        }
    }
}
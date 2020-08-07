package at.shockbytes.dante.ui.adapter.main

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.util.view.BookDiffUtilCallback
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import java.util.Collections

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 */
class BookAdapter(
        context: Context,
        private val imageLoader: ImageLoader,
        private val onOverflowActionClickedListener: (BookEntity) -> Unit,
        private val onLabelClickedListener: (BookLabel) -> Unit,
        private val randomPickCallback: RandomPickCallback,
        onItemClickListener: OnItemClickListener<BookAdapterEntity>,
        onItemMoveListener: OnItemMoveListener<BookAdapterEntity>
) : BaseAdapter<BookAdapterEntity>(
    context,
    onItemClickListener = onItemClickListener,
    onItemMoveListener = onItemMoveListener
), ItemTouchHelperAdapter {

    init {
        setHasStableIds(false)
    }

    override fun getItemId(position: Int): Long {
        return data[position].id
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BookAdapterEntity> {

        return when (viewType) {

            BookAdapterEntity.VIEW_TYPE_BOOK -> {
                BookViewHolder.forParent(
                        parent,
                        imageLoader,
                        onOverflowActionClickedListener,
                        onLabelClickedListener
                )
            }
            BookAdapterEntity.VIEW_TYPE_RANDOM_PICK -> {
                RandomPickViewHolder.forParent(
                        parent,
                        randomPickCallback
                )
            }
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
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

    fun updateData(books: List<BookAdapterEntity>) {
        val diffResult = DiffUtil.calculateDiff(BookDiffUtilCallback(data, books))

        data.clear()
        data.addAll(books)

        diffResult.dispatchUpdatesTo(this)
    }


}
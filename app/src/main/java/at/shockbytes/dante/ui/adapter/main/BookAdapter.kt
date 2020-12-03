package at.shockbytes.dante.ui.adapter.main

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.image.ImageLoader
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
    private val onLabelClickedListener: ((BookLabel) -> Unit)? = null,
    private val randomPickCallback: RandomPickCallback? = null,
    onItemClickListener: OnItemClickListener<BookAdapterItem>,
    onItemMoveListener: OnItemMoveListener<BookAdapterItem>
) : BaseAdapter<BookAdapterItem>(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BookAdapterItem> {

        return when (viewType) {

            BookAdapterItem.VIEW_TYPE_BOOK -> {
                BookViewHolder.forParent(
                    parent,
                    imageLoader,
                    onOverflowActionClickedListener,
                    onLabelClickedListener
                )
            }
            BookAdapterItem.VIEW_TYPE_RANDOM_PICK -> {
                RandomPickViewHolder.forParent(
                    parent,
                    randomPickCallback
                )
            }
            BookAdapterItem.VIEW_TYPE_EXPLANATION_WISHLIST -> {
                WishlistExplanationViewHolder.forParent(parent)
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

    fun updateData(books: List<BookAdapterItem>) {
        val diffResult = DiffUtil.calculateDiff(BookDiffUtilCallback(data, books))

        data.clear()
        data.addAll(books)

        diffResult.dispatchUpdatesTo(this)
    }
}
package at.shockbytes.dante.ui.adapter.main

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.R
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter.ViewHolder<BookAdapterEntity> {
        return BookViewHolder.fromView(
                inflater.inflate(R.layout.item_book, parent, false),
                imageLoader,
                onOverflowActionClickedListener,
                onLabelClickedListener
        )
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
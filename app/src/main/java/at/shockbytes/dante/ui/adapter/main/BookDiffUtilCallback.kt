package at.shockbytes.dante.ui.adapter.main

import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.core.isContentSame

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class BookDiffUtilCallback(
    private val oldList: List<BookAdapterItem>,
    private val newList: List<BookAdapterItem>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return when {

            oldItem is BookAdapterItem.Book && newItem is BookAdapterItem.Book -> {
                oldItem.bookEntity.isContentSame(newItem.bookEntity)
            }
            oldItem is BookAdapterItem.RandomPick && newItem is BookAdapterItem.RandomPick -> {
                // Both are objects, it's always true
                true
            }
            oldItem is BookAdapterItem.WishlistExplanation && newItem is BookAdapterItem.WishlistExplanation -> {
                // Both are objects, it's always true
                true
            }
            else -> {
                // If adapter entities don't match, content can't be the same
                false
            }
        }
    }
}
package at.shockbytes.dante.util.view

import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.core.isContentSame
import at.shockbytes.dante.ui.adapter.main.BookAdapterEntity

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class BookDiffUtilCallback(
        private val oldList: List<BookAdapterEntity>,
        private val newList: List<BookAdapterEntity>
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

            oldItem is BookAdapterEntity.Book && newItem is BookAdapterEntity.Book -> {
                oldItem.bookEntity.isContentSame(newItem.bookEntity)
            }
            oldItem is BookAdapterEntity.RandomPick && newItem is BookAdapterEntity.RandomPick -> {
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
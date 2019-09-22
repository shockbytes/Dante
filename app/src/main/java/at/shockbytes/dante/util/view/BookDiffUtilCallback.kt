package at.shockbytes.dante.util.view

import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.isContentSame

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
class BookDiffUtilCallback(
    private val oldList: List<BookEntity>,
    private val newList: List<BookEntity>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].isContentSame(newList[newItemPosition])
    }
}
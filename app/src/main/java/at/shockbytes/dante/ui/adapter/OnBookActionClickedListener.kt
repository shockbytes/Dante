package at.shockbytes.dante.ui.adapter

import at.shockbytes.dante.core.book.BookEntity

interface OnBookActionClickedListener {

    fun onDelete(book: BookEntity, onDeletionConfirmed: (Boolean) -> Unit)

    fun onShare(book: BookEntity)

    fun onMoveToUpcoming(book: BookEntity)

    fun onMoveToCurrent(book: BookEntity)

    fun onMoveToDone(book: BookEntity)
}
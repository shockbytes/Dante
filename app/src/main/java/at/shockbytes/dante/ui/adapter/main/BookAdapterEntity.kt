package at.shockbytes.dante.ui.adapter.main

import at.shockbytes.dante.core.book.BookEntity

sealed class BookAdapterEntity {

    abstract val id: Long
    abstract val viewType: Int

    data class Book(
        val bookEntity: BookEntity,
        override val viewType: Int = VIEW_TYPE_BOOK
    ) : BookAdapterEntity() {

        override val id: Long
            get() = bookEntity.id

        val title: String = bookEntity.title
    }

    object RandomPick : BookAdapterEntity() {

        override val id: Long = RANDOM_PICK_ID
        override val viewType: Int = VIEW_TYPE_RANDOM_PICK
    }

    companion object {

        const val RANDOM_PICK_ID = -1L
        const val VIEW_TYPE_BOOK = 1
        const val VIEW_TYPE_RANDOM_PICK = 2
    }
}
package at.shockbytes.dante.ui.adapter.main

import at.shockbytes.dante.core.book.BookEntity

sealed class BookAdapterItem {

    abstract val id: Long
    abstract val viewType: Int

    data class Book(
        val bookEntity: BookEntity,
        override val viewType: Int = VIEW_TYPE_BOOK
    ) : BookAdapterItem() {

        override val id: Long
            get() = bookEntity.id

        val title: String = bookEntity.title
    }

    object RandomPick : BookAdapterItem() {

        override val id: Long = RANDOM_PICK_ID
        override val viewType: Int = VIEW_TYPE_RANDOM_PICK
    }

    object WishlistExplanation : BookAdapterItem() {

        override val id: Long = EXPLANATION_WISHLIST_ID
        override val viewType: Int = VIEW_TYPE_EXPLANATION_WISHLIST
    }

    companion object {

        const val RANDOM_PICK_ID = -1L
        const val EXPLANATION_WISHLIST_ID = -2L
        const val VIEW_TYPE_BOOK = 1
        const val VIEW_TYPE_RANDOM_PICK = 2
        const val VIEW_TYPE_EXPLANATION_WISHLIST = 3
    }
}
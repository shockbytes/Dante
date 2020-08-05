package at.shockbytes.dante.ui.adapter.main

import at.shockbytes.dante.core.book.BookEntity

sealed class BookAdapterEntity {

    abstract val id: Long

    data class Book(
            val bookEntity: BookEntity
    ) : BookAdapterEntity() {

        override val id: Long
            get() = bookEntity.id

        val title: String = bookEntity.title
    }

    object RandomPick : BookAdapterEntity() {

        override val id: Long = RANDOM_PICK_ID
    }

    companion object {

        const val RANDOM_PICK_ID = -1L
    }
}
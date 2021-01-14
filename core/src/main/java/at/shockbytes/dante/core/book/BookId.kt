package at.shockbytes.dante.core.book

typealias BookId = Long

object BookIds {

    private const val DEFAULT_VALUE: BookId = -1L

    fun isValid(bookId: BookId): Boolean {
        return bookId > DEFAULT_VALUE
    }

    fun default() = DEFAULT_VALUE

}
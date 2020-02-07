package at.shockbytes.dante.stats

import at.shockbytes.dante.core.book.BareBoneBook

sealed class BookStatsItem {

    object BooksAndPages : BookStatsItem()

    sealed class ReadingDuration : BookStatsItem() {

        object Empty : ReadingDuration()

        data class Present(val slowest: BookWithDuration, val fastest: BookWithDuration) : ReadingDuration()
    }

    sealed class Favorites : BookStatsItem() {

        object Empty : Favorites()

        data class Present(
            val favoriteAuthor: FavoriteAuthor,
            val firstFiveStarBook: BareBoneBook
        ): Favorites()
    }

    /**
     * @param languages Occurrences of books in a certain language mapped to the language code
     */
    data class Languages(val languages: Map<String, Int>) : BookStatsItem()

    /**
     * Average rating
     * Average books per month
     * Most read month
     */
    object Others : BookStatsItem()
}
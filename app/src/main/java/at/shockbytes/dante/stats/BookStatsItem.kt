package at.shockbytes.dante.stats

import at.shockbytes.dante.core.book.BareBoneBook
import at.shockbytes.dante.core.book.Languages

sealed class BookStatsItem {

    sealed class BooksAndPages : BookStatsItem() {

        object Empty : BooksAndPages()

        data class Present(
            val booksAndPages: BooksPagesInfo
        ) : BooksAndPages()
    }

    sealed class ReadingDuration : BookStatsItem() {

        object Empty : ReadingDuration()

        data class Present(val slowest: BookWithDuration, val fastest: BookWithDuration) : ReadingDuration()
    }

    sealed class Favorites : BookStatsItem() {

        object Empty : Favorites()

        data class Present(
            val favoriteAuthor: FavoriteAuthor,
            val firstFiveStarBook: BareBoneBook?
        ) : Favorites()
    }

    /**
     * @param languages Occurrences of books in a certain language mapped to the language code
     */
    data class LanguageDistribution(val languages: Map<Languages, Int>) : BookStatsItem()

    sealed class Others : BookStatsItem() {

        object Empty : Others()

        data class Present(
            val averageRating: Double,
            val averageBooksPerMonth: Double,
            val mostActiveMonth: MostActiveMonth?
        ) : Others()
    }
}
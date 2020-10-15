package at.shockbytes.dante.core.book

sealed class ReadingGoal {

    data class PagesPerMonthReadingGoal(val pagesPerMonth: Int?) : ReadingGoal()

    data class BooksPerMonthReadingGoal(val booksPerMonth: Int?) : ReadingGoal()
}
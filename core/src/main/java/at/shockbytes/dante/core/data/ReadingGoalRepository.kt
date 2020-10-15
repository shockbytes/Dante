package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.ReadingGoal
import io.reactivex.Completable
import io.reactivex.Single

interface ReadingGoalRepository {

    fun retrievePagesPerMonthReadingGoal(): Single<ReadingGoal.PagesPerMonthReadingGoal>

    fun storePagesPerMonthReadingGoal(goal: Int): Completable

    fun resetPagesPerMonthReadingGoal(): Completable

    fun retrieveBookPerMonthReadingGoal(): Single<ReadingGoal.BooksPerMonthReadingGoal>

    fun storeBooksPerMonthReadingGoal(goal: Int): Completable

    fun resetBooksPerMonthReadingGoal(): Completable
}
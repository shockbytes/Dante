package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.ReadingGoal
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface ReadingGoalRepository {

    fun retrievePagesPerMonthReadingGoal(): Single<ReadingGoal.PagesPerMonthReadingGoal>

    fun storePagesPerMonthReadingGoal(goal: Int): Completable

    fun resetPagesPerMonthReadingGoal(): Completable

    fun retrieveBookPerMonthReadingGoal(): Single<ReadingGoal.BooksPerMonthReadingGoal>

    fun storeBooksPerMonthReadingGoal(goal: Int): Completable

    fun resetBooksPerMonthReadingGoal(): Completable
}
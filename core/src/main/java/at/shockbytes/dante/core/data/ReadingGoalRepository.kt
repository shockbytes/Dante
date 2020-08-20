package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.ReadingGoal
import io.reactivex.Single

interface ReadingGoalRepository {

    fun retrievePagesPerMonthReadingGoal(): Single<ReadingGoal.PagesPerMonthReadingGoal>

    fun retrieveBookPerMonthReadingGoal(): Single<ReadingGoal.BooksPerMonthReadingGoal>
}
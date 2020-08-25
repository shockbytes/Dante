package at.shockbytes.dante.core.data.local

import android.content.SharedPreferences
import at.shockbytes.dante.core.book.ReadingGoal
import at.shockbytes.dante.core.data.ReadingGoalRepository
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.getIntOrNullIfDefault
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.singleOf
import io.reactivex.Completable
import io.reactivex.Single

class SharedPrefsBackedReadingGoalRepository(
        private val sharedPreferences: SharedPreferences,
        private val schedulers: SchedulerFacade
) : ReadingGoalRepository {

    override fun retrievePagesPerMonthReadingGoal(): Single<ReadingGoal.PagesPerMonthReadingGoal> {
        return singleOf(subscribeOn = schedulers.io) {
            val readingGoal = sharedPreferences.getIntOrNullIfDefault(KEY_PAGES_READING_GOAL, DEFAULT_VALUE)
            ReadingGoal.PagesPerMonthReadingGoal(readingGoal)
        }
    }

    override fun storePagesPerMonthReadingGoal(goal: Int): Completable {
        return completableOf(subscribeOn = schedulers.io) {
            sharedPreferences.edit().putInt(KEY_PAGES_READING_GOAL, goal).apply()
        }
    }

    override fun resetPagesPerMonthReadingGoal(): Completable {
        return storePagesPerMonthReadingGoal(DEFAULT_VALUE)
    }

    override fun retrieveBookPerMonthReadingGoal(): Single<ReadingGoal.BooksPerMonthReadingGoal> {
        return singleOf(subscribeOn = schedulers.io) {
            val readingGoal = sharedPreferences.getIntOrNullIfDefault(KEY_BOOKS_READING_GOAL, DEFAULT_VALUE)
            ReadingGoal.BooksPerMonthReadingGoal(readingGoal)
        }
    }

    companion object {
        private const val KEY_PAGES_READING_GOAL = "key_pages_reading_goal"
        private const val KEY_BOOKS_READING_GOAL = "key_books_reading_goal"

        private const val DEFAULT_VALUE = -1
    }
}
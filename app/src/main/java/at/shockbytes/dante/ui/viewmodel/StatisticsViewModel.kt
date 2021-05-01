package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.core.book.ReadingGoal
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.core.data.ReadingGoalRepository
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.stats.BookStatsBuilder
import at.shockbytes.dante.ui.adapter.stats.model.ReadingGoalType
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function4
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val recordDao: PageRecordDao,
    private val readingGoalRepository: ReadingGoalRepository,
    private val schedulers: SchedulerFacade
) : BaseViewModel() {

    private data class ZipContent(
        val books: List<BookEntity>,
        val records: List<PageRecord>,
        val pagesReadingGoal: ReadingGoal.PagesPerMonthReadingGoal,
        val booksReadingGoal: ReadingGoal.BooksPerMonthReadingGoal
    )

    private val zipper = Function4 { books: List<BookEntity>,
                                     records: List<PageRecord>,
                                     pagesReadingGoal: ReadingGoal.PagesPerMonthReadingGoal,
                                     booksReadingGoal: ReadingGoal.BooksPerMonthReadingGoal ->
        ZipContent(books, records, pagesReadingGoal, booksReadingGoal)
    }

    sealed class ReadingGoalState {

        abstract val goalType: ReadingGoalType

        sealed class Present : ReadingGoalState() {

            abstract val goal: Int

            data class Pages(
                override val goal: Int,
                override val goalType: ReadingGoalType = ReadingGoalType.PAGES
            ) : Present()

            data class Books(
                override val goal: Int,
                override val goalType: ReadingGoalType = ReadingGoalType.BOOKS
            ) : Present()
        }

        data class Absent(
            val defaultGoal: Int,
            override val goalType: ReadingGoalType
        ) : ReadingGoalState()
    }

    private val statisticsItems = MutableLiveData<List<BookStatsViewItem>>()
    fun getStatistics(): LiveData<List<BookStatsViewItem>> = statisticsItems

    private val pageGoalChangeEvent = PublishSubject.create<ReadingGoalState>()
    fun onPageGoalChangeRequest(): Observable<ReadingGoalState> = pageGoalChangeEvent

    fun requestStatistics() {
        Observable
            .zip(
                bookRepository.bookObservable,
                recordDao.allPageRecords(),
                readingGoalRepository.retrievePagesPerMonthReadingGoal().toObservable(),
                readingGoalRepository.retrieveBookPerMonthReadingGoal().toObservable(),
                zipper
            )
            .map { (books, pageRecords, pagesReadingGoal, booksReadingGoal) ->
                BookStatsBuilder.build(books, pageRecords, pagesReadingGoal, booksReadingGoal)
            }
            .subscribe(statisticsItems::postValue, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun requestPageGoalChangeAction(type: ReadingGoalType) {
        goalChangeObservableSourceByType(type)
            .observeOn(schedulers.ui)
            .subscribe(pageGoalChangeEvent::onNext)
            .addTo(compositeDisposable)
    }

    private fun goalChangeObservableSourceByType(type: ReadingGoalType): Single<ReadingGoalState> {
        return when (type) {
            ReadingGoalType.PAGES -> {
                readingGoalRepository.retrievePagesPerMonthReadingGoal()
                    .map { goal ->
                        if (goal.pagesPerMonth != null) {
                            ReadingGoalState.Present.Pages(goal.pagesPerMonth!!)
                        } else {
                            ReadingGoalState.Absent(PAGES_DEFAULT_GOAL, ReadingGoalType.PAGES)
                        }
                    }
            }
            ReadingGoalType.BOOKS -> {
                readingGoalRepository.retrieveBookPerMonthReadingGoal()
                    .map { goal ->
                        if (goal.booksPerMonth != null) {
                            ReadingGoalState.Present.Books(goal.booksPerMonth!!)
                        } else {
                            ReadingGoalState.Absent(BOOKS_DEFAULT_GOAL, ReadingGoalType.BOOKS)
                        }
                    }
            }
        }
    }

    fun onGoalPicked(readingGoal: Int, goalType: ReadingGoalType) {
        getGoalStorageSource(readingGoal, goalType)
            .observeOn(schedulers.ui)
            .subscribe(::requestStatistics)
            .addTo(compositeDisposable)
    }

    private fun getGoalStorageSource(readingGoal: Int, goalType: ReadingGoalType): Completable {
        return when (goalType) {
            ReadingGoalType.PAGES -> readingGoalRepository.storePagesPerMonthReadingGoal(readingGoal)
            ReadingGoalType.BOOKS -> readingGoalRepository.storeBooksPerMonthReadingGoal(readingGoal)
        }
    }

    fun onGoalDeleted(goalType: ReadingGoalType) {
        getGoalResetSource(goalType)
            .observeOn(schedulers.ui)
            .subscribe(::requestStatistics)
            .addTo(compositeDisposable)
    }

    private fun getGoalResetSource(goalType: ReadingGoalType): Completable {
        return when (goalType) {
            ReadingGoalType.PAGES -> readingGoalRepository.resetPagesPerMonthReadingGoal()
            ReadingGoalType.BOOKS -> readingGoalRepository.resetBooksPerMonthReadingGoal()
        }
    }

    companion object {

        private const val PAGES_DEFAULT_GOAL = 600
        private const val BOOKS_DEFAULT_GOAL = 4
    }
}
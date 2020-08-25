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
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.Observable
import io.reactivex.functions.Function4
import io.reactivex.rxkotlin.addTo
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

    private val zipper = Function4 {
        books: List<BookEntity>,
        records: List<PageRecord>,
        pagesReadingGoal: ReadingGoal.PagesPerMonthReadingGoal,
        booksReadingGoal: ReadingGoal.BooksPerMonthReadingGoal ->
        ZipContent(books, records, pagesReadingGoal, booksReadingGoal)
    }

    sealed class ReadingGoalState {

        data class Present(val goal: Int): ReadingGoalState()

        data class Absent(val defaultGoal: Int) : ReadingGoalState()
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

    fun requestPageGoalChangeAction() {
        readingGoalRepository.retrievePagesPerMonthReadingGoal()
                .map { goal ->
                    if (goal.pagesPerMonth != null) {
                        ReadingGoalState.Present(goal.pagesPerMonth!!)
                    } else {
                        ReadingGoalState.Absent(PAGES_DEFAULT_GOAL)
                    }
                }
                .subscribe(pageGoalChangeEvent::onNext)
                .addTo(compositeDisposable)
    }

    fun onPagesGoalPicked(pagesReadingGoal: Int) {
        readingGoalRepository.storePagesPerMonthReadingGoal(pagesReadingGoal)
                .observeOn(schedulers.ui)
                .subscribe(::requestStatistics)
                .addTo(compositeDisposable)
    }

    fun onPagesGoalDeleted() {
        readingGoalRepository.resetPagesPerMonthReadingGoal()
                .observeOn(schedulers.ui)
                .subscribe(::requestStatistics)
                .addTo(compositeDisposable)
    }

    companion object {

        private const val PAGES_DEFAULT_GOAL = 600
    }
}
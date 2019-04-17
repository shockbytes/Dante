package at.shockbytes.dante.ui.fragment

import android.app.DatePickerDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.palette.graphics.Palette
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.dialog.SimpleRequestDialogFragment
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.ui.image.ImageLoadingCallback
import at.shockbytes.dante.ui.viewmodel.BookDetailViewModel
import at.shockbytes.dante.util.AnimationUtils
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_book_detail.*
import kotlinx.android.synthetic.main.fragment_book_detail_legacy.*
import org.joda.time.DateTime
import ru.bullyboo.view.CircleSeekBar
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    02.02.2019
 */
class BookDetailFragment : BaseFragment(), BackAnimatable, ImageLoadingCallback,
        androidx.palette.graphics.Palette.PaletteAsyncListener, CircleSeekBar.Callback {

    override val layoutId = R.layout.fragment_book_detail

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: BookDetailViewModel

    private val animatableViewsList: List<View> by lazy {
        listOf(
            sb_detail_pages,
            btn_detail_pages,
            btn_detail_rate,
            btn_detail_notes,
            btn_detail_published,
            view_detail_date_divider,
            btn_detail_wishhlist_date,
            btn_detail_start_date,
            btn_detail_end_date
        )
    }

    private val iconLoadingMap: List<Pair<Int, TextView>> by lazy {
        listOf(
            Pair(R.drawable.ic_published_date, btn_detail_published),
            Pair(R.drawable.ic_rating, btn_detail_rate),
            Pair(R.drawable.ic_pages, btn_detail_pages),
            Pair(R.drawable.ic_notes, btn_detail_notes),
            Pair(R.drawable.ic_popup_upcoming, btn_detail_wishhlist_date),
            Pair(R.drawable.ic_popup_current, btn_detail_start_date),
            Pair(R.drawable.ic_popup_done, btn_detail_end_date)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this, vmFactory)[BookDetailViewModel::class.java]
        arguments?.getLong(ARG_BOOK_ID)?.let { bookId -> viewModel.initializeWithBookId(bookId) }
    }

    override fun setupViews() {
        setupViewListener()

        // Collapse / unfold summary when clicking on it
        txt_detail_description.setOnClickListener {

            val defaultLines = resources.getInteger(R.integer.detail_summary_default_lines)
            var lines = txt_detail_description.maxLines
            lines = if (lines == defaultLines) 100 else defaultLines
            txt_detail_description.maxLines = lines
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        // TODO Version 3.4
        // inflater?.inflate(R.menu.popup_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun bindViewModel() {
        viewModel.getViewState().observe(this, androidx.lifecycle.Observer { viewState ->
            viewState?.let {
                initializeBookInformation(viewState.book, viewState.showSummary)
                initializeTimeInformation(viewState.book)
            }
        })

        viewModel.showBookFinishedDialogEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { title ->
                    SimpleRequestDialogFragment.newInstance(getString(R.string.book_finished, title),
                            getString(R.string.book_finished_move_to_done_question), R.drawable.ic_pick_done)
                            .setOnAcceptListener {
                                viewModel.moveBookToDone()
                                activity?.supportFinishAfterTransition()
                            }
                            .show(fragmentManager, "book-finished-dialogfragment")
                }
                .addTo(compositeDisposable)

        viewModel.showPagesDialogEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data ->
                    data?.let { (currentPage, pageCount, _) ->
                        fragmentManager?.let { fm ->
                            val fragment = PagesFragment.newInstance(currentPage, pageCount).apply {
                                onPageEditedListener = { current, pages ->
                                    viewModel.updateBookPages(current, pages)
                                }
                            }
                            DanteUtils.addFragmentToActivity(fm, fragment, android.R.id.content, true)
                        }
                    }
                }
                .addTo(compositeDisposable)

        viewModel.showNotesDialogEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data ->
                    data?.let { (title, thumbnailAddress, notes) ->

                        fragmentManager?.let { fm ->
                            val fragment = NotesFragment.newInstance(title, thumbnailAddress, notes)
                                    .apply {
                                        onSavedClickListener = { updatedNotes ->
                                            viewModel.updateNotes(updatedNotes)
                                            setupNotes(notes.isEmpty())
                                        }
                                    }
                            DanteUtils.addFragmentToActivity(fm, fragment, android.R.id.content, true)
                        }
                    }
                }
                .addTo(compositeDisposable)

        viewModel.showRatingDialogEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { data ->
                    fragmentManager?.let { fm ->
                        val fragment = RateFragment.newInstance(data)
                                .apply {
                                    onRateClickListener = { rating ->
                                        viewModel.updateRating(rating)
                                        btn_detail_rate?.text = resources.getQuantityString(R.plurals.book_rating, rating, rating)
                                    }
                                }
                        DanteUtils.addFragmentToActivity(fm, fragment, android.R.id.content, true)
                    }
                }
                .addTo(compositeDisposable)
    }

    override fun unbindViewModel() {
        viewModel.getViewState().removeObservers(this)
    }

    override fun onBackwardAnimation() {
        animatableViewsList.forEach { v ->
            v.clearAnimation()
            v.alpha = 0f
        }
    }

    override fun onImageResourceReady(resource: Drawable?) {
        (resource as? BitmapDrawable)?.bitmap?.let { bm ->
            androidx.palette.graphics.Palette.from(bm).generate(this)
        }
    }

    override fun onResume() {
        super.onResume()

        context?.let { ctx ->
            loadIcons(ctx)
        }
    }

    override fun onImageLoadingFailed(e: Exception?) {
        Timber.d(e)
    }

    override fun onGenerated(palette: androidx.palette.graphics.Palette?) {

        val actionBarColor = palette?.lightMutedSwatch?.rgb
        val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette?.darkMutedSwatch?.rgb

        (activity as? TintableBackNavigableActivity)?.tintSystemBarsWithText(actionBarColor,
                actionBarTextColor, statusBarColor)
    }

    override fun onStartScrolling(startValue: Int) = Unit

    override fun onEndScrolling(endValue: Int) {
        viewModel.updateCurrentPage(endValue)
    }

    // --------------------------------------------------------------------

    private fun initializeBookInformation(book: BookEntity, showSummary: Boolean) {

        txt_detail_title.text = book.title
        txt_detail_author.text = book.author

        btn_detail_published.text = if (!book.publishedDate.isEmpty()) book.publishedDate else "---"
        btn_detail_rate.text = if (book.rating in 1..5) {
            resources.getQuantityString(R.plurals.book_rating, book.rating, book.rating)
        } else getString(R.string.rate_book)

        // Hide subtitle if not available
        txt_detail_subtitle.setVisible(book.subTitle.isNotEmpty())
        txt_detail_subtitle.text = book.subTitle

        txt_detail_description.setVisible(book.summary != null && showSummary)
        txt_detail_description.text = book.summary

        if (!book.thumbnailAddress.isNullOrEmpty()) {
            loadImage(book.thumbnailAddress)
        }

        setupNotes(book.notes.isNullOrEmpty())
        setupPageComponents(book.state, book.reading, book.hasPages, book.pageCount, book.currentPage)
    }

    private fun setupViewListener() {

        btn_detail_pages.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.requestPageDialog()
        }
        btn_detail_published.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showDatePicker(DATE_TARGET_PUBLISHED_DATE)
        }
        btn_detail_notes.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.requestNotesDialog()
        }
        btn_detail_rate.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.requestRatingDialog()
        }
        btn_detail_wishhlist_date.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showDatePicker(DATE_TARGET_WISHLIST_DATE)
        }
        btn_detail_start_date.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showDatePicker(DATE_TARGET_START_DATE)
        }
        btn_detail_end_date.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showDatePicker(DATE_TARGET_END_DATE)
        }
    }

    private fun loadIcons(ctx: Context) {
        Observable
                .fromIterable(iconLoadingMap)
                .map { (drawableRes, v) ->
                    Pair(DanteUtils.vector2Drawable(ctx, drawableRes), v)
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (drawable, v) ->
                    v.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                }, { throwable ->
                    throwable.printStackTrace()
                }, {
                    // In the end start the component animations in onComplete()
                    startComponentAnimations()
                })
                .addTo(compositeDisposable)
    }

    private fun startComponentAnimations() {
        AnimationUtils.detailEnterAnimation(
            animatableViewsList,
            duration = 200,
            initialDelay = 550,
            interpolator = AccelerateDecelerateInterpolator()
        )
    }

    private fun showDatePicker(target: Int) {
        context?.let { ctx ->
            val cal = Calendar.getInstance()
            DatePickerDialog(ctx,
                    { _, y, m, d ->

                        when (target) {
                            DATE_TARGET_PUBLISHED_DATE -> {
                                onUpdatePublishedDate(y.toString(), m.plus(1).toString(), d.toString()) // +1 because month starts with 0
                            }
                            DATE_TARGET_WISHLIST_DATE -> {
                                val wishlistDate = DanteUtils.buildTimestampFromDate(y, m, d)
                                if (!viewModel.updateWishlistDate(wishlistDate)) {
                                    showToast(R.string.invalid_time_range_wishlist, true)
                                }
                            }
                            DATE_TARGET_START_DATE -> {
                                val startDate = DanteUtils.buildTimestampFromDate(y, m, d)
                                if (!viewModel.updateStartDate(startDate)) {
                                    showToast(R.string.invalid_time_range_start, true)
                                }
                            }
                            DATE_TARGET_END_DATE -> {
                                val endDate = DanteUtils.buildTimestampFromDate(y, m, d)
                                if (!viewModel.updateEndDate(endDate)) {
                                    showToast(R.string.invalid_time_range_end, true)
                                }
                            }
                        }
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
    }

    private fun initializeTimeInformation(book: BookEntity) {

        // Hide complete card if no time information is available
        if (!book.isAnyTimeInformationAvailable) {
            layoutDetailFragmentDates.visibility = View.GONE
        } else {

            val pattern = "dd. MMM yyyy"
            // Check if wishlist date is available
            if (book.wishlistDate > 0) {
                val wishlistDate = DateTime(book.wishlistDate).toString(pattern)
                btn_detail_wishhlist_date.text = wishlistDate
            } else {
                btn_detail_wishhlist_date.visibility = View.INVISIBLE
            }

            // Check if start date is available
            if (book.startDate > 0) {
                val startDate = DateTime(book.startDate).toString(pattern)
                btn_detail_start_date.text = startDate
            } else {
                btn_detail_start_date.visibility = View.INVISIBLE
            }

            // Check if end date is available
            if (book.endDate > 0) {
                val endDate = DateTime(book.endDate).toString(pattern)
                btn_detail_end_date.text = endDate
            } else {
                btn_detail_end_date.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupPageComponents(
        state: BookState,
        isReading: Boolean,
        hasPages: Boolean,
        pageCount: Int,
        currentPage: Int
    ) {
        // Book must be in reading state and must have a legit page count and overall the feature
        // must be enabled in the settings
        if (isReading && hasPages) {

            sb_detail_pages.maxValue = pageCount
            sb_detail_pages.value = currentPage
            sb_detail_pages.setCallback(this)
            sb_detail_pages.setOnValueChangedListener { page ->
                btn_detail_pages.text = getString(R.string.detail_pages, page, pageCount)
            }

            // Show pages as button text
            val pages = if (state == BookState.READING)
                getString(R.string.detail_pages, currentPage, pageCount)
            else
                pageCount.toString()
            btn_detail_pages.text = pages
        } else {
            sb_detail_pages.apply {
                visibility = View.INVISIBLE
                isEnabled = false
            }
            btn_detail_pages.text = pageCount.toString()
        }
    }

    private fun setupNotes(isNotesEmpty: Boolean) {
        val notesId = if (!isNotesEmpty) R.string.my_notes else R.string.add_notes
        btn_detail_notes.text = getString(notesId)
    }

    private fun loadImage(address: String?) {
        if (address != null) {
            activity?.let { ctx ->

                // TODO Enable 2 times zoom
                // val increasedZoomUrl = DownloadUtils.increaseGoogleThumbnailResolutionLink(url, 2)

                imageLoader.loadImageWithCornerRadius(
                        ctx,
                        address,
                        iv_detail_image,
                        cornerDimension = ctx.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt(),
                        callback = this,
                        callbackHandleValues = Pair(first = false, second = true))
            }
        } else {
            iv_detail_image.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun onUpdatePublishedDate(y: String, m: String, d: String) {

        val mStr = m.padStart(2, '0')
        val dStr = d.padStart(2, '0')
        val publishedDate = "$y-$mStr-$dStr"

        btn_detail_published.text = publishedDate

        viewModel.updatePublishedDate(publishedDate)
    }

    companion object {

        // Const callback values for time picker
        private const val DATE_TARGET_PUBLISHED_DATE = 1
        private const val DATE_TARGET_WISHLIST_DATE = 2
        private const val DATE_TARGET_START_DATE = 3
        private const val DATE_TARGET_END_DATE = 4

        private const val ARG_BOOK_ID = "arg_book_id"

        fun newInstance(id: Long): BookDetailFragment {
            return BookDetailFragment().apply {
                this.arguments = Bundle().apply {
                    putLong(ARG_BOOK_ID, id)
                }
            }
        }
    }
}
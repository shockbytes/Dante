package at.shockbytes.dante.ui.fragment

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.support.v7.widget.AppCompatDrawableManager
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.dialog.NotesDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.PageEditDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.RateBookDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.SimpleRequestDialogFragment
import at.shockbytes.dante.ui.viewmodel.BookDetailViewModel
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.tracking.Tracker
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_book_detail.*
import org.joda.time.DateTime
import ru.bullyboo.view.CircleSeekBar
import java.util.*
import javax.inject.Inject

/**
 * @author  Martin Macheiner
 * Date:    08-Jun-18.
 */
class BookDetailFragment : BaseFragment(), RequestListener<Drawable>,
        Palette.PaletteAsyncListener, CircleSeekBar.Callback {

    override val layoutId = R.layout.fragment_book_detail

    @Inject
    protected lateinit var settings: DanteSettings

    @Inject
    protected lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    protected lateinit var tracker: Tracker

    private val animationList: List<View> by lazy {
        listOf(btnDetailFragmentPublished, btnDetailFragmentRating,
                circleSeekbarDetailFragmentPages, btnDetailFragmentPages, btnDetailFragmentNotes,
                viewDetailFragmentDivider, btnDetailFragmentWishlistDate, btnDetailFragmentStartDate,
                btnDetailFragmentEndDate)
    }

    private val drawableResList: List<Pair<Int, TextView>> by lazy {
        listOf(Pair(R.drawable.ic_published_date, btnDetailFragmentPublished),
                Pair(R.drawable.ic_rating, btnDetailFragmentRating),
                Pair(R.drawable.ic_pages, btnDetailFragmentPages),
                Pair(R.drawable.ic_notes, btnDetailFragmentNotes),
                Pair(R.drawable.ic_popup_upcoming, btnDetailFragmentWishlistDate),
                Pair(R.drawable.ic_popup_current, btnDetailFragmentStartDate),
                Pair(R.drawable.ic_popup_done, btnDetailFragmentEndDate))
    }

    private lateinit var viewModel: BookDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[BookDetailViewModel::class.java]

        arguments?.getLong(ARG_BOOK_ID)?.let { bookId -> viewModel.bookId = bookId }
    }

    override fun setupViews() {
        setupViewListener()
        setupObserver()
    }

    override fun onResume() {
        super.onResume()
        loadIcons()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
        return true
    }

    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        (resource as? BitmapDrawable)?.bitmap?.let { bm ->
            Palette.from(bm).generate(this)
        }
        return false
    }

    override fun onGenerated(palette: Palette) {

        val actionBarColor = palette.lightMutedSwatch?.rgb
        val actionBarTextColor = palette.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette.darkMutedSwatch?.rgb

        (activity as? TintableBackNavigableActivity)?.tintSystemBarsWithText(actionBarColor,
                actionBarTextColor, statusBarColor)
    }

    override fun onStartScrolling(startValue: Int) {}

    override fun onEndScrolling(endValue: Int) {
        viewModel.updateCurrentPage(endValue)
    }

    // --------------------------------------------------------------------

    fun backwardAnimation() {
        animationList.forEach { it.alpha = 0f }
    }

    // --------------------------------------------------------------------

    private fun setupObserver() {

        viewModel.book.observe(this, android.arch.lifecycle.Observer {

            it?.let { book ->

                activity?.title = book.title
                initializeBookInformation(book)
                initializeTimeInformation(book)
            }
        })

        viewModel.showBookFinishedDialog.observe(this, Observer { title ->
            SimpleRequestDialogFragment.newInstance(getString(R.string.book_finished, title),
                    getString(R.string.book_finished_move_to_done_question), R.drawable.ic_pick_done)
                    .setOnAcceptListener {
                        viewModel.moveBookToDone()
                        activity?.supportFinishAfterTransition()
                    }
                    .show(fragmentManager, "book-finished-dialogfragment")
        })

        viewModel.showPagesDialog.observe(this, Observer { data ->
            data?.let { (currentPage, pageCount, isReading) ->
                // Only show current page in dialog if tracking is enabled and book is in reading state
                val showCurrent = settings.pageTrackingEnabled and isReading
                PageEditDialogFragment.newInstance(currentPage, pageCount, showCurrent)
                        .setOnPageEditedListener { current, pages ->
                            viewModel.updateBookPages(current, pages)
                        }.show(fragmentManager, "pages-dialogfragment")
            }
        })

        viewModel.showNotesDialog.observe(this, Observer { data ->
            data?.let { (title, thumbnailAddress, notes) ->
                NotesDialogFragment.newInstance(title, thumbnailAddress, notes)
                        .setOnApplyListener { updatedNotes ->
                            viewModel.updateNotes(updatedNotes)
                            setupNotes(notes.isEmpty())
                        }.show(fragmentManager, "notes-dialogfragment")
            }
        })

        viewModel.showRatingDialog.observe(this, Observer { data ->
            data?.let { (title, thumbnailAddress, r) ->
                RateBookDialogFragment.newInstance(title, thumbnailAddress, r)
                        .setOnApplyListener { rating ->
                            viewModel.updateRating(rating)
                            tracker.trackRatingEvent(rating)
                            btnDetailFragmentRating.text = resources.getQuantityString(R.plurals.book_rating, rating, rating)
                        }.show(fragmentManager, "rating-dialogfragment")
            }
        })

    }

    private fun setupViewListener() {

        btnDetailFragmentPages.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.requestPageDialog()
        }
        btnDetailFragmentPublished.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showDatePicker(DATE_TARGET_PUBLISHED_DATE)
        }
        btnDetailFragmentNotes.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.requestNotesDialog()
        }
        btnDetailFragmentRating.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.requestRatingDialog()
        }

        btnDetailFragmentWishlistDate.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showDatePicker(DATE_TARGET_WISHLIST_DATE)
        }
        btnDetailFragmentStartDate.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showDatePicker(DATE_TARGET_START_DATE)
        }
        btnDetailFragmentEndDate.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showDatePicker(DATE_TARGET_END_DATE)
        }
    }

    private fun initializeBookInformation(book: BookEntity) {

        txtDetailFragmentTitle.text = book.title
        txtDetailFragmentAuthor.text = book.author

        btnDetailFragmentPublished.text = if (!book.publishedDate.isEmpty()) book.publishedDate else "---"
        btnDetailFragmentRating.text = if (book.rating in 1..5) {
            resources.getQuantityString(R.plurals.book_rating, book.rating, book.rating)
        } else getString(R.string.rate_book)

        // Hide subtitle if not available
        val subtitle = book.subTitle
        if (subtitle.isEmpty()) {
            txtDetailFragmentSubtitle.visibility = View.GONE
        } else {
            txtDetailFragmentSubtitle.text = subtitle
        }

        if (!book.thumbnailAddress.isNullOrEmpty()) {
            loadImage(book.thumbnailAddress)
        }

        setupNotes(book.notes.isNullOrEmpty())
        setupPageComponents(book.state, book.reading, book.hasPages, book.pageCount, book.currentPage)
    }

    private fun loadIcons() {
        Observable.fromCallable {
            drawableResList.mapNotNull { (drawableRes, view) ->
                context?.let { ctx ->
                    val drawable = AppCompatDrawableManager.get().getDrawable(ctx, drawableRes)
                    Pair(drawable, view)
                }
            }.toList()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ list ->
            list.forEach { (drawable, view) ->
                view.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            }
        }, { throwable -> throwable.printStackTrace() }, {
            // In the end start the component animations
            startComponentAnimations()
        })
    }

    private fun showDatePicker(target: Int) {
        val cal = Calendar.getInstance()
        DatePickerDialog(activity,
                { _, y, m, d ->

                    when (target) {
                        DATE_TARGET_PUBLISHED_DATE -> {
                            onUpdatePublishedDate(y.toString(), m.plus(1).toString(), d.toString()) // +1 because month starts with 0
                        }
                        DATE_TARGET_WISHLIST_DATE -> {
                            val wishlistDate = buildTimestampFromDate(y, m, d)
                            if (!viewModel.updateWishlistDate(wishlistDate)) {
                                showToast(R.string.invalid_time_range_wishlist, true)
                            }
                        }
                        DATE_TARGET_START_DATE -> {
                            val startDate = buildTimestampFromDate(y, m, d)
                            if (!viewModel.updateStartDate(startDate)) {
                                showToast(R.string.invalid_time_range_start, true)
                            }
                        }
                        DATE_TARGET_END_DATE -> {
                            val endDate = buildTimestampFromDate(y, m, d)
                            if (!viewModel.updateEndDate(endDate)) {
                                showToast(R.string.invalid_time_range_end, true)
                            }
                        }
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show()
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
                btnDetailFragmentWishlistDate.text = wishlistDate
            } else {
                btnDetailFragmentWishlistDate.visibility = View.INVISIBLE
            }

            // Check if start date is available
            if (book.startDate > 0) {
                val startDate = DateTime(book.startDate).toString(pattern)
                btnDetailFragmentStartDate.text = startDate
            } else {
                btnDetailFragmentStartDate.visibility = View.INVISIBLE
            }

            // Check if end date is available
            if (book.endDate > 0) {
                val endDate = DateTime(book.endDate).toString(pattern)
                btnDetailFragmentEndDate.text = endDate
            } else {
                btnDetailFragmentEndDate.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupPageComponents(state: BookState, isReading: Boolean, hasPages: Boolean,
                                    pageCount: Int, currentPage: Int) {
        // Book must be in reading state and must have a legit page count and overall the feature
        // must be enabled in the settings
        if (settings.pageTrackingEnabled && isReading && hasPages) {

            circleSeekbarDetailFragmentPages.maxValue = pageCount
            circleSeekbarDetailFragmentPages.value = currentPage
            circleSeekbarDetailFragmentPages.setCallback(this)
            circleSeekbarDetailFragmentPages.setOnValueChangedListener { page ->
                btnDetailFragmentPages.text = getString(R.string.detail_pages, page, pageCount)
            }

            // Show pages as button text
            val pages = if (state == BookState.READING)
                getString(R.string.detail_pages, currentPage, pageCount)
            else
                pageCount.toString()
            btnDetailFragmentPages.text = pages

        } else {
            circleSeekbarDetailFragmentPages.visibility = View.GONE
            // Show all pages, but disable button clicking
            btnDetailFragmentPages.text = pageCount.toString()
        }
    }

    private fun startComponentAnimations() {
        DanteUtils.listPopAnimation(animationList, 200, 550, AccelerateDecelerateInterpolator())
    }

    private fun setupNotes(isNotesEmpty: Boolean) {
        val notesId = if (!isNotesEmpty) R.string.my_notes else R.string.add_notes
        btnDetailFragmentNotes.text = getString(notesId)
    }

    private fun loadImage(address: String?) {
        activity?.let { ctx ->
            Glide.with(ctx)
                    .load(address)
                    .apply(RequestOptions().placeholder(R.drawable.ic_placeholder))
                    .listener(this)
                    .into(imgViewDetailFragmentThumbnail)
        }
    }

    private fun onUpdatePublishedDate(y: String, m: String, d: String) {

        val mStr = m.padStart(2, '0')
        val dStr = d.padStart(2, '0')
        val publishedDate = "$y-$mStr-$dStr"

        btnDetailFragmentPublished.text = publishedDate

        viewModel.updatePublishedDate(publishedDate)
    }

    private fun buildTimestampFromDate(y: Int, m: Int, d: Int): Long {

        val cal = Calendar.getInstance()

        cal.set(Calendar.YEAR, y)
        cal.set(Calendar.MONTH, m)
        cal.set(Calendar.DAY_OF_MONTH, d)
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.timeInMillis
    }

    // --------------------------------------------------------------------

    companion object {

        private const val DATE_TARGET_PUBLISHED_DATE = 1
        private const val DATE_TARGET_WISHLIST_DATE = 2
        private const val DATE_TARGET_START_DATE = 3
        private const val DATE_TARGET_END_DATE = 4

        private const val ARG_BOOK_ID = "arg_book_id"

        fun newInstance(bookId: Long): BookDetailFragment {
            val fragment = BookDetailFragment()
            val args = Bundle()
            args.putLong(ARG_BOOK_ID, bookId)
            fragment.arguments = args
            return fragment
        }

    }

}
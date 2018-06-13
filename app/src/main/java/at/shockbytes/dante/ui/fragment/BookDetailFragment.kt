package at.shockbytes.dante.ui.fragment

import android.app.DatePickerDialog
import android.graphics.drawable.BitmapDrawable
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
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.dialog.NotesDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.PageEditDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.RateBookDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.SimpleRequestDialogFragment
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.tracking.Tracker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
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
class BookDetailFragment : BaseFragment(), Callback,
        Palette.PaletteAsyncListener, CircleSeekBar.Callback {

    override val layoutId = R.layout.fragment_book_detail

    @Inject
    protected lateinit var bookDao: BookEntityDao

    @Inject
    protected lateinit var settings: DanteSettings

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

    private lateinit var book: BookEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bookId = arguments?.getLong(ARG_BOOK_ID)
        if (bookId != null) {
            bookDao.get(bookId)?.let {
                book = it
            }
        } else {
            showToast(R.string.error_load_book)
            activity?.supportFinishAfterTransition()
        }
    }

    override fun setupViews() {

        btnDetailFragmentPages.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            // Only show current page in dialog if tracking is enabled and book is in reading state
            val showCurrentPage = settings.pageTrackingEnabled and book.reading
            PageEditDialogFragment.newInstance(book.currentPage, book.pageCount, showCurrentPage)
                    .setOnPageEditedListener { current, pages ->

                        book.currentPage = current
                        book.pageCount = pages
                        bookDao.update(book)
                        setupPageComponents()
                    }.show(fragmentManager, "pages-dialogfragment")
        }
        btnDetailFragmentPublished.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val cal = Calendar.getInstance()
            DatePickerDialog(activity,
                    { _, y, m, d -> onUpdatePublishedDate(y.toString(), m.plus(1).toString(), d.toString()) }, // +1 because month starts with 0
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
        btnDetailFragmentNotes.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            NotesDialogFragment.newInstance(book.title, book.thumbnailAddress, book.notes ?: "")
                    .setOnApplyListener { notes ->

                        book.notes = notes
                        bookDao.update(book)
                        setupNotes()
                    }.show(fragmentManager, "notes-dialogfragment")
        }
        btnDetailFragmentRating.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            RateBookDialogFragment.newInstance(book.title, book.thumbnailAddress, book.rating)
                    .setOnApplyListener { rating ->

                        book.rating = rating
                        bookDao.update(book)
                        tracker.trackRatingEvent(rating)
                        btnDetailFragmentRating.text = resources.getQuantityString(R.plurals.book_rating, rating, rating)
                    }.show(fragmentManager, "rating-dialogfragment")
        }

        // TODO Open dates DialogFragment
        btnDetailFragmentWishlistDate.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showToast("Change wishlist date")
        }
        btnDetailFragmentStartDate.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showToast("Change start date")
        }
        btnDetailFragmentEndDate.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showToast("Change end date")
        }

        // Initialize after the views are set up
        initialize()
    }

    override fun onResume() {
        super.onResume()
        loadIcons()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onSuccess() {
        (imgViewDetailFragmentThumbnail.drawable as? BitmapDrawable)?.bitmap?.let { bm ->
            Palette.from(bm).generate(this)
        }
    }

    override fun onError() {}

    override fun onGenerated(palette: Palette) {

        val actionBarColor = palette.lightMutedSwatch?.rgb
        val actionBarTextColor = palette.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette.darkMutedSwatch?.rgb

        (activity as? TintableBackNavigableActivity)?.tintSystemBarsWithText(actionBarColor,
                actionBarTextColor, statusBarColor, book.title)
    }

    override fun onStartScrolling(startValue: Int) {}

    override fun onEndScrolling(endValue: Int) {

        book.currentPage = endValue
        bookDao.update(book)
        if (book.currentPage == book.pageCount) {
            SimpleRequestDialogFragment.newInstance(getString(R.string.book_finished, book.title),
                    getString(R.string.book_finished_move_to_done_question), R.drawable.ic_pick_done)
                    .setOnAcceptListener {

                        book.updateState(BookState.READ)
                        bookDao.update(book)
                        activity?.supportFinishAfterTransition()
                    }
                    .show(fragmentManager, "book-finished-dialogfragment")
        }
    }

    // --------------------------------------------------------------------

    fun backwardAnimation() {
        animationList.forEach { it.alpha = 0f }
    }

    // --------------------------------------------------------------------

    private fun initialize() {
        initializeBookInformation()
        initializeTimeInformation()
    }

    private fun initializeBookInformation() {

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

        setupNotes()
        setupPageComponents()
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

    private fun initializeTimeInformation() {

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

    private fun setupPageComponents() {
        // Book must be in reading state and must have a legit page count and overall the feature
        // must be enabled in the settings
        if (settings.pageTrackingEnabled && book.reading && book.hasPages) {

            circleSeekbarDetailFragmentPages.maxValue = book.pageCount
            circleSeekbarDetailFragmentPages.value = book.currentPage
            circleSeekbarDetailFragmentPages.setCallback(this)
            circleSeekbarDetailFragmentPages.setOnValueChangedListener { page ->
                btnDetailFragmentPages.text = getString(R.string.detail_pages, page, book.pageCount)
            }

            // Show pages as button text
            val pages = if (book.state == BookState.READING)
                getString(R.string.detail_pages, book.currentPage, book.pageCount)
            else
                book.pageCount.toString()
            btnDetailFragmentPages.text = pages

        } else {
            circleSeekbarDetailFragmentPages.visibility = View.GONE
            // Show all pages, but disable button clicking
            btnDetailFragmentPages.text = book.pageCount.toString()
        }
    }

    private fun startComponentAnimations() {
        DanteUtils.listPopAnimation(animationList, 200, 550, AccelerateDecelerateInterpolator())
    }

    private fun setupNotes() {
        val notesId = if (!book.notes.isNullOrEmpty()) R.string.my_notes else R.string.add_notes
        btnDetailFragmentNotes.text = getString(notesId)
    }

    private fun loadImage(address: String?) {
        Picasso.with(activity)
                .load(address)
                .placeholder(R.drawable.ic_placeholder)
                .into(imgViewDetailFragmentThumbnail, this)
    }

    private fun onUpdatePublishedDate(y: String, m: String, d: String) {

        val mStr = m.padStart(2, '0')
        val dStr = d.padStart(2, '0')
        val publishedDate = "$y-$mStr-$dStr"

        btnDetailFragmentPublished.text = publishedDate

        book.publishedDate = publishedDate
        bookDao.update(book)
    }

    // --------------------------------------------------------------------

    companion object {

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
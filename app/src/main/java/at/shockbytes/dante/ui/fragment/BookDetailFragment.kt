package at.shockbytes.dante.ui.fragment

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.palette.graphics.Palette
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.BookIds
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.image.ImageLoadingCallback
import at.shockbytes.dante.databinding.FragmentBookDetailBinding
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.activity.NotesActivity
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPageRecordDataPoint
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramAction
import at.shockbytes.dante.ui.custom.bookspages.BooksAndPagesDiagramOptions
import at.shockbytes.dante.ui.custom.bookspages.MarkerViewLabelFactory
import at.shockbytes.dante.ui.viewmodel.BookDetailViewModel
import at.shockbytes.dante.ui.view.AnimationUtils
import at.shockbytes.dante.ui.view.ChipFactory
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.DanteUtils.dpToPixelF
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.registerForPopupMenu
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import ru.bullyboo.view.CircleSeekBar
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    02.02.2019
 */
class BookDetailFragment : BaseFragment<FragmentBookDetailBinding>(),
    BackAnimatable,
    ImageLoadingCallback,
    CircleSeekBar.Callback {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentBookDetailBinding {
        return FragmentBookDetailBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: BookDetailViewModel

    private var menuItemEdit: MenuItem? = null

    private val animatableViewsList: List<View> by lazy {
        listOf(
            vb.ivDetailImage,
            vb.txtDetailTitle,
            vb.txtDetailSubtitle,
            vb.txtDetailAuthor,
            vb.txtDetailDescription,
            vb.sbDetailPages,
            vb.btnDetailPages,
            vb.btnDetailRate,
            vb.btnDetailNotes,
            vb.btnDetailPublished,
            vb.dividerBookDetailsExtras,
            vb.pagesDiagramView,
            vb.hsvLabels,
            vb.layoutDetailDates,
            vb.btnDetailWishhlistDate,
            vb.btnDetailStartDate,
            vb.btnDetailEndDate
        )
    }

    private val iconLoadingMap: List<Pair<Int, TextView>> by lazy {
        listOf(
            Pair(R.drawable.ic_published_date, vb.btnDetailPublished),
            Pair(R.drawable.ic_rating, vb.btnDetailRate),
            Pair(R.drawable.ic_pages, vb.btnDetailPages),
            Pair(R.drawable.ic_notes, vb.btnDetailNotes),
            Pair(R.drawable.ic_tab_upcoming, vb.btnDetailWishhlistDate),
            Pair(R.drawable.ic_tab_current, vb.btnDetailStartDate),
            Pair(R.drawable.ic_tab_done, vb.btnDetailEndDate)
        )
    }

    private val notesReceiver = object : BroadcastReceiver() {

        override fun onReceive(p0: Context?, data: Intent?) {

            val updatedNotes = data?.extras?.getString(NotesActivity.NOTES_EXTRA) ?: return
            viewModel.updateNotes(updatedNotes)
            setupNotes(updatedNotes.isEmpty())
        }
    }

    private val bookUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, data: Intent?) {
            viewModel.reload()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = viewModelOf(vmFactory)
        arguments?.getLong(ARG_BOOK_ID, BookIds.default())
            ?.let(viewModel::initializeWithBookId)

        registerLocalBroadcastReceiver()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        return inflater.inflate(R.menu.menu_book_detail, menu).also {
            menuItemEdit = menu.findItem(R.id.menu_book_detail_edit)
        }
    }

    private fun registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(requireContext()).apply {
            registerReceiver(notesReceiver, IntentFilter(NotesActivity.ACTION_NOTES))
            registerReceiver(bookUpdatedReceiver, IntentFilter(ACTION_BOOK_CHANGED))
        }
    }

    override fun setupViews() {
        setupViewListener()

        // Collapse / unfold summary when clicking on it
        vb.txtDetailDescription.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val defaultLines = resources.getInteger(R.integer.detail_summary_default_lines)
            val currentLines = vb.txtDetailDescription.maxLines

            val lines = if (currentLines == defaultLines) 100 else defaultLines
            val animationDuration = if (currentLines == defaultLines) 250L else 100L

            ObjectAnimator.ofInt(
                vb.txtDetailDescription,
                "maxLines",
                lines
            ).apply {
                duration = animationDuration
                start()
            }
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.getViewState().observe(this, { viewState ->
            initializeBookInformation(viewState.book, viewState.showSummary)
            initializeTimeInformation(viewState.book)
        })

        viewModel.getLabels().observe(this, Observer(::setupLabels))

        viewModel.getPageRecordsViewState().observe(this, Observer(::handlePageRecordViewState))

        viewModel.onBookDetailEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleBookDetailEvent, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)

        viewModel.showBookFinishedDialogEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { title ->
                MaterialDialog(requireContext())
                    .title(text = getString(R.string.book_finished, title))
                    .message(R.string.book_finished_move_to_done_question)
                    .icon(R.drawable.ic_pick_done)
                    .cornerRadius(requireContext().dpToPixelF(6))
                    .positiveButton(android.R.string.ok) {
                        viewModel.moveBookToDone()
                        activity?.supportFinishAfterTransition()
                    }
                    .show()
            }
            .addTo(compositeDisposable)

        viewModel.showPagesDialogEvent
            .observeOn(AndroidSchedulers.mainThread())
            .map(::createPagesFragment)
            .subscribe { fragment ->
                DanteUtils.addFragmentToActivity(parentFragmentManager, fragment, android.R.id.content, true)
            }
            .addTo(compositeDisposable)

        viewModel.showNotesDialogEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { data ->
                ActivityNavigator.navigateTo(context, Destination.Notes(data))
            }
            .addTo(compositeDisposable)

        viewModel.showRatingDialogEvent
            .observeOn(AndroidSchedulers.mainThread())
            .map(::createRateFragment)
            .subscribe { fragment ->
                DanteUtils.addFragmentToActivity(parentFragmentManager, fragment, android.R.id.content, true)
            }
            .addTo(compositeDisposable)

        viewModel.onBookEditRequest
            .observeOn(AndroidSchedulers.mainThread())
            .map(Destination::ManualAdd)
            .subscribe({ destination ->

                val sceneTransition = activity?.let {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(it)
                }?.toBundle()
                ActivityNavigator.navigateTo(context, destination, sceneTransition)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)

        viewModel.onAddLabelsRequest
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::showLabelPicker, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun createPagesFragment(pageInfo: BookDetailViewModel.PageInfo): PagesFragment {
        val (currentPage, pageCount) = pageInfo
        return PagesFragment.newInstance(currentPage, pageCount).apply {
            onPageEditedListener = { current, pages ->
                viewModel.updateBookPages(current, pages)
            }
        }
    }

    private fun createRateFragment(data: BookDetailViewModel.RatingInfo): RateFragment {
        return RateFragment.newInstance(data)
            .apply {
                onRateClickListener = { rating ->
                    viewModel.updateRating(rating)
                    vb.btnDetailRate.text = resources.getQuantityString(R.plurals.book_rating, rating, rating)
                }
            }
    }

    override fun unbindViewModel() {
        viewModel.getViewState().removeObservers(this)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).apply {
            unregisterReceiver(notesReceiver)
            unregisterReceiver(bookUpdatedReceiver)
        }

        viewModel.onFragmentDestroyed()

        super.onDestroy()
    }

    override fun onBackwardAnimation() {
        animatableViewsList.forEach { v ->
            v.clearAnimation()
            v.alpha = 0f
        }
    }

    override fun onStart() {
        super.onStart()
        loadIcons()
    }

    override fun onImageLoadingFailed(e: Exception?) {
        Timber.d(e)
    }

    override fun onImageResourceReady(resource: Drawable?) {
        (resource as? BitmapDrawable)?.bitmap
            ?.let(Palette::from)
            ?.generate(::onPaletteGenerated)
    }

    private fun onPaletteGenerated(palette: Palette?) {

        palette?.lightMutedSwatch?.titleTextColor?.let { textColor ->
            tintEditMenuItem(textColor)
        }

        val actionBarColor = palette?.lightMutedSwatch?.rgb
        val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette?.darkMutedSwatch?.rgb

        (activity as? TintableBackNavigableActivity<*>)
            ?.tintSystemBarsWithText(actionBarColor, actionBarTextColor, statusBarColor)
    }

    private fun tintEditMenuItem(@ColorInt tintColor: Int) {
        menuItemEdit?.icon?.let { icon ->
            val drawable = DrawableCompat.wrap(icon)
            DrawableCompat.setTint(drawable, tintColor)
            menuItemEdit?.setIcon(drawable)
        }
    }

    override fun onStartScrolling(startValue: Int) = Unit

    override fun onEndScrolling(endValue: Int) {
        viewModel.updateCurrentPage(endValue)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_book_detail_edit) {
            viewModel.requestEditBook()
        }
        return super.onOptionsItemSelected(item)
    }

    // --------------------------------------------------------------------

    private fun initializeBookInformation(book: BookEntity, showSummary: Boolean) {

        vb.txtDetailTitle.text = book.title
        vb.txtDetailAuthor.text = book.author

        vb.btnDetailPublished.text = if (book.publishedDate.isNotEmpty()) book.publishedDate else "---"
        vb.btnDetailRate.text = if (book.rating in 1..5) {
            resources.getQuantityString(R.plurals.book_rating, book.rating, book.rating)
        } else getString(R.string.rate_book)

        // Hide subtitle if not available
        vb.txtDetailSubtitle.apply {
            setVisible(book.subTitle.isNotEmpty())
            text = book.subTitle
        }

        vb.txtDetailDescription.apply {
            setVisible(book.summary != null && showSummary)
            text = book.summary
        }

        loadImage(book.normalizedThumbnailUrl)

        setupNotes(book.notes.isNullOrEmpty())
        setupPageComponents(book.state, book.reading, book.hasPages, book.pageCount, book.currentPage)
    }

    private fun handlePageRecordViewState(
        pageRecordViewState: BookDetailViewModel.PageRecordsViewState
    ) {
        when (pageRecordViewState) {
            is BookDetailViewModel.PageRecordsViewState.Present -> {
                vb.groupDetailsPages.setVisible(true)
                handlePageRecords(pageRecordViewState.dataPoints, pageRecordViewState.bookId)
            }
            BookDetailViewModel.PageRecordsViewState.Absent -> {
                vb.groupDetailsPages.setVisible(false)
            }
        }
    }

    private fun handleBookDetailEvent(event: BookDetailViewModel.BookDetailEvent) {
        when (event) {
            is BookDetailViewModel.BookDetailEvent.Message -> showToast(event.msgRes)
        }
    }

    private fun handlePageRecords(
        dataPoints: List<BooksAndPageRecordDataPoint>,
        bookId: BookId
    ) {
        vb.pagesDiagramView.apply {
            setData(
                dataPoints,
                diagramOptions = BooksAndPagesDiagramOptions(initialZero = true),
                labelFactory = MarkerViewLabelFactory.ofBooksAndPageRecordDataPoints(dataPoints, R.string.pages_formatted)
            )
            action = BooksAndPagesDiagramAction.Overflow
            registerOnActionClick {
                showPageRecordsOverview(bookId)
            }
            headerTitle = getString(R.string.reading_behavior)
        }
    }

    private fun showPageRecordsOverview(bookId: BookId) {
        registerForPopupMenu(
            vb.pagesDiagramView.actionView,
            R.menu.menu_page_records_details
        ) { item ->
            when (item.itemId) {
                R.id.menu_page_records_details -> {
                    DanteUtils.addFragmentToActivity(
                        parentFragmentManager,
                        PageRecordsDetailFragment.newInstance(bookId),
                        android.R.id.content,
                        addToBackStack = true
                    )
                }
                R.id.menu_page_records_reset -> {
                    MaterialDialog(requireContext()).show {
                        icon(R.drawable.ic_delete)
                        title(text = getString(R.string.ask_for_all_page_record_deletion_title))
                        message(text = getString(R.string.ask_for_all_page_record_deletion_msg))
                        positiveButton(R.string.action_delete) {
                            viewModel.deleteAllPageRecords()
                        }
                        negativeButton(android.R.string.cancel) {
                            dismiss()
                        }
                        cancelOnTouchOutside(false)
                        cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
                    }
                }
            }
            true
        }
    }

    private fun setupViewListener() {

        vb.btnDetailPages.setHapticClickListener(viewModel::requestPageDialog)
        vb.btnDetailPublished.setHapticClickListener { showDatePicker(DATE_TARGET_PUBLISHED_DATE) }
        vb.btnDetailNotes.setHapticClickListener(viewModel::requestNotesDialog)
        vb.btnDetailRate.setHapticClickListener(viewModel::requestRatingDialog)
        vb.btnDetailWishhlistDate.setHapticClickListener { showDatePicker(DATE_TARGET_WISHLIST_DATE) }
        vb.btnDetailStartDate.setHapticClickListener { showDatePicker(DATE_TARGET_START_DATE) }
        vb.btnDetailEndDate.setHapticClickListener { showDatePicker(DATE_TARGET_END_DATE) }
        vb.btnAddLabel.setHapticClickListener(viewModel::requestAddLabels)
    }

    private fun View.setHapticClickListener(action: () -> Unit) {
        this.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            action()
        }
    }

    private fun loadIcons() {
        Observable
            .fromIterable(iconLoadingMap)
            .map { (drawableRes, v) ->
                Pair(DanteUtils.vector2Drawable(requireContext(), drawableRes), v)
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            // In the end (onComplete) start the component animations in onComplete()
            .subscribe(::setCompoundDrawable, ExceptionHandlers::defaultExceptionHandler, ::startComponentAnimations)
            .addTo(compositeDisposable)
    }

    private fun setCompoundDrawable(pair: Pair<Drawable, TextView>) {

        val (drawable, view) = pair
        view.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
    }

    private fun startComponentAnimations() {
        AnimationUtils.detailEnterAnimation(
            animatableViewsList,
            duration = 300,
            initialDelay = 550,
            durationBetweenAnimations = 15,
            interpolator = AccelerateDecelerateInterpolator(),
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
                            val wishListDate = DanteUtils.buildTimestampFromDate(y, m, d)
                            if (!viewModel.updateWishlistDate(wishListDate)) {
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

    private fun showLabelPicker(alreadyAttachedLabels: List<BookLabel>) {

        LabelPickerBottomSheetFragment
            .newInstance(alreadyAttachedLabels)
            .setOnLabelSelectedListener(viewModel::attachLabel)
            .show(childFragmentManager, "pick-label-bottom-sheet")
    }

    private fun initializeTimeInformation(book: BookEntity) {

        // Hide complete card if no time information is available
        if (!book.isAnyTimeInformationAvailable) {
            vb.layoutDetailDates.visibility = View.GONE
        } else {

            val pattern = "dd. MMM yyyy"
            // Wishlist Date
            when {
                book.wishlistDate > 0 -> vb.btnDetailWishhlistDate.text = DateTime(book.wishlistDate).toString(pattern)
                book.state != BookState.READ_LATER -> vb.btnDetailWishhlistDate.setText(R.string.not_available)
                else -> vb.btnDetailWishhlistDate.visibility = View.INVISIBLE
            }

            // Start Date
            when {
                book.startDate > 0 -> vb.btnDetailStartDate.text = DateTime(book.startDate).toString(pattern)
                book.state != BookState.READ_LATER -> vb.btnDetailStartDate.setText(R.string.not_available)
                else -> vb.btnDetailStartDate.visibility = View.INVISIBLE
            }

            // End Date
            when {
                book.endDate > 0 -> vb.btnDetailEndDate.text = DateTime(book.endDate).toString(pattern)
                else -> vb.btnDetailEndDate.visibility = View.INVISIBLE
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

            setupPageProgress(currentPage, pageCount)

            vb.btnDetailPages.text = if (state == BookState.READING)
            // Initially set currentPage to 0 for progress animation
                getString(R.string.detail_pages, currentPage, pageCount)
            else
                pageCount.toString()
        } else {
            vb.sbDetailPages.apply {
                visibility = View.INVISIBLE
                isEnabled = false
            }
            vb.btnDetailPages.text = pageCount.toString()
        }
    }

    private fun setupPageProgress(currentPage: Int, pageCount: Int) {

        vb.sbDetailPages.apply {
            value = currentPage
            maxValue = pageCount
            setCallback(this@BookDetailFragment)
            setOnValueChangedListener { page ->
                vb.btnDetailPages.text = getString(R.string.detail_pages, page, pageCount)
            }
        }
    }

    private fun setupNotes(isNotesEmpty: Boolean) {
        val notesId = if (!isNotesEmpty) R.string.my_notes else R.string.add_notes
        vb.btnDetailNotes.text = getString(notesId)
    }

    private fun loadImage(address: String?) {
        if (!address.isNullOrEmpty()) {
            imageLoader.loadImageWithCornerRadius(
                requireContext(),
                address,
                vb.ivDetailImage,
                cornerDimension = requireContext().resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt(),
                callback = this,
                callbackHandleValues = Pair(first = false, second = true))
        } else {
            tintEditMenuItem(ContextCompat.getColor(requireContext(), R.color.danteAccent))
            vb.ivDetailImage.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun setupLabels(labels: List<BookLabel>) {

        vb.chipsDetailLabel.removeAllViews()

        labels
            .map { label ->
                ChipFactory.buildChipViewFromLabel(
                    requireContext(),
                    label,
                    onLabelClickedListener = null,
                    showCloseIcon = true,
                    closeIconClickCallback = viewModel::removeLabel
                )
            }
            .forEach(vb.chipsDetailLabel::addView)
    }

    private fun onUpdatePublishedDate(y: String, m: String, d: String) {

        val mStr = m.padStart(2, '0')
        val dStr = d.padStart(2, '0')
        val publishedDate = "$y-$mStr-$dStr"

        vb.btnDetailPublished.text = publishedDate

        viewModel.updatePublishedDate(publishedDate)
    }

    companion object {

        const val ACTION_BOOK_CHANGED = "action_book_changed"

        // Const callback values for time picker
        private const val DATE_TARGET_PUBLISHED_DATE = 1
        private const val DATE_TARGET_WISHLIST_DATE = 2
        private const val DATE_TARGET_START_DATE = 3
        private const val DATE_TARGET_END_DATE = 4

        private const val ARG_BOOK_ID = "arg_book_id"

        fun newInstance(id: BookId): BookDetailFragment {
            return BookDetailFragment().apply {
                this.arguments = Bundle().apply {
                    putLong(ARG_BOOK_ID, id)
                }
            }
        }
    }
}
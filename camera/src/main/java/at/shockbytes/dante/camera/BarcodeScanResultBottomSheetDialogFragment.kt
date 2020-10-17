package at.shockbytes.dante.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import at.shockbytes.dante.camera.injection.DaggerCameraComponent
import at.shockbytes.dante.camera.viewmodel.BarcodeResultViewModel
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLoadingState
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.book.BookSuggestion
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.injection.CoreInjectHelper
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_barcode_scan_bottom_sheet.*
import timber.log.Timber
import javax.inject.Inject

class BarcodeScanResultBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var isbn: String
    private var askForAnotherScan: Boolean = false
    private var showNotMyBookButton: Boolean = true

    private var closeListener: (() -> Unit)? = null
    private var onBookAddedListener: ((CharSequence) -> Unit)? = null

    private lateinit var viewModel: BarcodeResultViewModel

    override fun getTheme() = R.style.BottomSheetDialogTheme

    @Inject
    lateinit var booksDownloader: BookDownloader

    @Inject
    lateinit var schedulers: SchedulerFacade

    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIntoCameraComponent()

        isbn = arguments?.getString(ARG_BARCODE_ISBN)
            ?: throw IllegalStateException("ISBN argument must be not null!")
        askForAnotherScan = arguments?.getBoolean(ARG_ASK_FOR_ANOTHER_SCAN, false) ?: false
        showNotMyBookButton = arguments?.getBoolean(ARG_SHOW_NOT_MY_BOOK_BUTTON, true) ?: true

        viewModel = BarcodeResultViewModel(booksDownloader, schedulers, bookRepository)
        viewModel.loadBook(isbn)
    }

    private fun injectIntoCameraComponent() {
        DaggerCameraComponent
            .builder()
            .coreComponent(CoreInjectHelper.provideCoreComponent(requireActivity().applicationContext))
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barcode_scan_bottom_sheet, container, false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getBookLoadingState().observe(this, Observer { state ->
            when (state) {
                is BookLoadingState.Loading -> showLoadingLayout()
                is BookLoadingState.Error -> showErrorLayout(getString(state.cause))
                is BookLoadingState.Success -> showSuccessLayout(state.bookSuggestion)
            }
        })

        viewModel.onBookStoredEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleBookStoredEvent, Timber::e)
            .addTo(compositeDisposable)
    }

    private fun handleBookStoredEvent(event: BarcodeResultViewModel.BookStoredEvent) {
        when (event) {
            is BarcodeResultViewModel.BookStoredEvent.Success -> {
                if (askForAnotherScan) {
                    showBookStoredDialog(event.title)
                } else {
                    dismiss()
                }
            }
            is BarcodeResultViewModel.BookStoredEvent.Error -> {
                Toast.makeText(context, event.reason, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showBookStoredDialog(storedBook: String) {
        MaterialDialog(requireContext()).show {
            title(text = getString(R.string.book_added_to_library, storedBook))
            message(R.string.scan_another_book)
            positiveButton(R.string.yes) {
                this@BarcodeScanResultBottomSheetDialogFragment.dismiss()
            }
            negativeButton(R.string.no) {
                resetCloseListener()
                activity?.supportFinishAfterTransition()
            }
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    /**
     * Reset the close listener because the hosting activity is going to be destroyed anyway. This
     * will prevent the app to reopen the camera and therefore to crash.
     */
    private fun resetCloseListener() {
        closeListener = null
    }

    private fun showSuccessLayout(bookSuggestion: BookSuggestion) {
        layout_barcode_result_error.setVisible(false)
        pb_barcode_result.setVisible(false)
        group_barcode_result.setVisible(true)
        btn_barcode_result_not_my_book.setVisible(showNotMyBookButton)

        bookSuggestion.mainSuggestion?.run {
            tv_barcode_result_title.text = title
            tv_barcode_result_author.text = author

            thumbnailAddress?.let { imageUrl ->
                imageLoader.loadImageWithCornerRadius(
                    requireContext(),
                    imageUrl,
                    iv_barcode_scan_result_cover,
                    cornerDimension = AppUtils.convertDpInPixel(6, requireContext())
                )
            }

            btn_barcode_result_for_later.setOnClickListener {
                viewModel.storeBook(this, state = BookState.READ_LATER)
                onBookAddedListener?.invoke(title)
            }

            btn_barcode_result_reading.setOnClickListener {
                viewModel.storeBook(this, state = BookState.READING)
                onBookAddedListener?.invoke(title)
            }

            btn_barcode_result_read.setOnClickListener {
                viewModel.storeBook(this, state = BookState.READ)
                onBookAddedListener?.invoke(title)
            }
        }

        btn_barcode_result_not_my_book.setOnClickListener {
            showOtherSuggestionsModal(bookSuggestion.otherSuggestions) { selectedBook ->
                viewModel.setSelectedBook(bookSuggestion, selectedBook)
            }
        }
    }

    private fun showOtherSuggestionsModal(suggestions: List<BookEntity>, selectionListener: (BookEntity) -> Unit) {

        MaterialDialog(requireContext()).show {
            title(R.string.download_suggestion_header_other)
            customListAdapter(
                BookSuggestionPickerAdapter(
                    requireContext(),
                    suggestions,
                    imageLoader,
                    onItemClickListener = object : BaseAdapter.OnItemClickListener<BookEntity> {
                        override fun onItemClick(content: BookEntity, position: Int, v: View) {
                            selectionListener(content)
                            dismiss()
                        }
                    }
                )
            )
            positiveButton(R.string.nope) {
                dismiss()
            }
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    private fun showErrorLayout(cause: String) {
        pb_barcode_result.setVisible(false)
        group_barcode_result.setVisible(false)
        layout_barcode_result_error.setVisible(true)
        btn_barcode_result_not_my_book.setVisible(false)

        tv_barcode_result_error_cause.text = cause
        btn_barcode_result_error_close.setOnClickListener {
            dismiss()
        }
    }

    private fun showLoadingLayout() {
        pb_barcode_result.setVisible(true)
        group_barcode_result.setVisible(false)
        layout_barcode_result_error.setVisible(false)
        btn_barcode_result_not_my_book.setVisible(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent.parent.parent as View).fitsSystemWindows = false
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        closeListener?.invoke()
        super.onDestroy()
    }

    fun setOnCloseListener(function: () -> Unit): BarcodeScanResultBottomSheetDialogFragment {
        return this.apply {
            closeListener = function
        }
    }

    fun setOnBookAddedListener(function: (CharSequence) -> Unit): BarcodeScanResultBottomSheetDialogFragment {
        return this.apply {
            onBookAddedListener = function
        }
    }

    companion object {

        private const val ARG_BARCODE_ISBN = "arg_barcode_isbn"
        private const val ARG_ASK_FOR_ANOTHER_SCAN = "arg_ask_for_another_scan"
        private const val ARG_SHOW_NOT_MY_BOOK_BUTTON = "arg_show_not_my_book_button"

        fun newInstance(
            isbn: String,
            askForAnotherScan: Boolean,
            showNotMyBookButton: Boolean = true
        ): BarcodeScanResultBottomSheetDialogFragment {
            return BarcodeScanResultBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BARCODE_ISBN, isbn)
                    putBoolean(ARG_ASK_FOR_ANOTHER_SCAN, askForAnotherScan)
                    putBoolean(ARG_SHOW_NOT_MY_BOOK_BUTTON, showNotMyBookButton)
                }
            }
        }
    }
}
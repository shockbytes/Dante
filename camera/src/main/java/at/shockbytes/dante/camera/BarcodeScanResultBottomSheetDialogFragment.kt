package at.shockbytes.dante.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import at.shockbytes.dante.camera.injection.DaggerCameraComponent
import at.shockbytes.dante.camera.viewmodel.BarcodeResultViewModel
import at.shockbytes.dante.core.book.BookLoadingState
import at.shockbytes.dante.core.book.BookSuggestion
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.injection.CoreInjectHelper
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_barcode_scan_bottom_sheet.*
import javax.inject.Inject

class BarcodeScanResultBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var isbn: String

    private var closeListener: (() -> Unit)? = null

    private lateinit var viewModel: BarcodeResultViewModel

    @Inject
    lateinit var booksDownloader: BookDownloader

    @Inject
    lateinit var schedulers: SchedulerFacade

    @Inject
    lateinit var bookDao: BookEntityDao

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIntoCameraComponent()

        isbn = arguments?.getString("isbn") ?: throw IllegalStateException("ISBN must be not null!")
        viewModel = BarcodeResultViewModel(booksDownloader, schedulers, bookDao)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        btn_barcode_result_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getBookLoadingState().observe(this, Observer { state ->

            when (state) {
                BookLoadingState.Loading -> {
                    showLoadingLayout()
                }
                BookLoadingState.Error -> {
                    showErrorLayout()
                }
                is BookLoadingState.Success -> {
                    showSuccessLayout(state.bookSuggestion)
                }
            }
        })
    }

    private fun showSuccessLayout(bookSuggestion: BookSuggestion) {
        bookSuggestion.mainSuggestion?.run {
            tv_barcode_result_title.text = title
            tv_barcode_result_author.text = author

            thumbnailAddress?.let { imageUrl ->
                imageLoader.loadImage(requireContext(), imageUrl, iv_barcode_scan_result_cover)
            }
        }
    }

    private fun showErrorLayout() {
    }

    private fun showLoadingLayout() {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent.parent.parent as View).fitsSystemWindows = false
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

    companion object {

        fun newInstance(isbn: String): BarcodeScanResultBottomSheetDialogFragment {
            return BarcodeScanResultBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("isbn", isbn)
                }
            }
        }
    }
}
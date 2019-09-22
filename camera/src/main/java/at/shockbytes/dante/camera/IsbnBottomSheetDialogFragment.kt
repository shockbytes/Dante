package at.shockbytes.dante.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.camera.injection.DaggerCameraComponent
import at.shockbytes.dante.camera.viewmodel.IsbnResolverViewModel
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.injection.CoreInjectHelper
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.isbn_bottom_sheet.*
import javax.inject.Inject

class IsbnBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var isbn: String

    private var closeListener: (() -> Unit)? = null

    private lateinit var viewModel: IsbnResolverViewModel

    @Inject
    lateinit var booksDownloader: BookDownloader

    @Inject
    lateinit var schedulers: SchedulerFacade

    @Inject
    lateinit var bookDao: BookEntityDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIntoCameraComponent()

        isbn = arguments?.getString("isbn") ?: throw IllegalStateException("ISBN must be not null!")
        viewModel = IsbnResolverViewModel(booksDownloader, schedulers, bookDao)
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
        return inflater.inflate(R.layout.isbn_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_bottom_sheet_isbn.text = isbn
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent.parent.parent as View).fitsSystemWindows = false
    }

    override fun onDestroy() {
        closeListener?.invoke()
        super.onDestroy()
    }

    fun setOnCloseListener(function: () -> Unit): IsbnBottomSheetDialogFragment {
        return this.apply {
            closeListener = function
        }
    }

    companion object {

        fun newInstance(isbn: String): IsbnBottomSheetDialogFragment {
            return IsbnBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("isbn", isbn)
                }
            }
        }
    }
}
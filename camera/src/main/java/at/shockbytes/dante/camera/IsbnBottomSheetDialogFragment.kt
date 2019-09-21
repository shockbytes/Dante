package at.shockbytes.dante.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.camera.dependencies.CameraDependencyProvider.provideBooksDownloader
import at.shockbytes.dante.camera.dependencies.CameraDependencyProvider.provideSchedulers
import at.shockbytes.dante.camera.viewmodel.IsbnResolverViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.isbn_bottom_sheet.*

class IsbnBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var isbn: String

    private var closeListener: (() -> Unit)? = null

    private lateinit var viewModel: IsbnResolverViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isbn = arguments?.getString("isbn") ?: throw IllegalStateException("ISBN must be not null!")
        viewModel = IsbnResolverViewModel(provideBooksDownloader(), provideSchedulers())
        viewModel.loadBook(isbn)
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
        (view!!.parent.parent.parent as View).fitsSystemWindows = false
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
package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.viewmodel.PageRecordsDetailViewModel
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.dante.R
import kotlinx.android.synthetic.main.fragment_page_records_details.*
import javax.inject.Inject

class PageRecordsDetailFragment : BaseFragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private var bookId: Long by argument()

    private lateinit var viewModel: PageRecordsDetailViewModel

    override val layoutId: Int = R.layout.fragment_page_records_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {
        // TODO
        btn_page_records_details_close.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        layout_page_records_details.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.initialize(bookId)
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(bookId: Long): PageRecordsDetailFragment {
            return PageRecordsDetailFragment().apply {
                this.bookId = bookId
            }
        }
    }
}
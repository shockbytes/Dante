package at.shockbytes.dante.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.viewmodel.PageRecordsDetailViewModel
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookId
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.databinding.FragmentPageRecordsDetailsBinding
import at.shockbytes.dante.ui.adapter.pagerecords.PageRecordsAdapter
import at.shockbytes.dante.util.addTo
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import javax.inject.Inject

class PageRecordsDetailFragment : BaseFragment<FragmentPageRecordsDetailsBinding>() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private var bookId: BookId by argument()

    private lateinit var viewModel: PageRecordsDetailViewModel

    private val recordsAdapter: PageRecordsAdapter by lazy {
        PageRecordsAdapter(requireContext(), ::askForEntryDeletionConfirmation)
    }

    private fun askForEntryDeletionConfirmation(pageRecord: PageRecord) {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_delete)
            title(text = getString(R.string.ask_for_page_record_deletion_title))
            message(text = getString(R.string.ask_for_page_record_deletion_msg))
            positiveButton(R.string.action_delete) {
                viewModel.deletePageRecord(pageRecord)
            }
            negativeButton(android.R.string.cancel) {
                dismiss()
            }
            cancelOnTouchOutside(false)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }


    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentPageRecordsDetailsBinding {
        return FragmentPageRecordsDetailsBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {
        vb.btnPageRecordsDetailsClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        vb.layoutPageRecordsDetails.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        vb.rvPageRecordsDetails.adapter = recordsAdapter
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.initialize(bookId)

        viewModel.getRecords().observe(this, Observer(recordsAdapter::updateData))

        viewModel.onBookChangedEvent()
                .subscribe(::sendBookChangedBroadcast)
                .addTo(compositeDisposable)
    }

    private fun sendBookChangedBroadcast(unused: Unit) {
        LocalBroadcastManager.getInstance(requireContext())
                .sendBroadcast(Intent(BookDetailFragment.ACTION_BOOK_CHANGED))
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(bookId: BookId): PageRecordsDetailFragment {
            return PageRecordsDetailFragment().apply {
                this.bookId = bookId
            }
        }
    }
}
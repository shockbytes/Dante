package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.ui.viewmodel.BookDetailViewModel
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.tracking.Tracker
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    02.02.2019
 */
class BookDetailFragment : BaseFragment(), BackAnimatable {

    override val layoutId = R.layout.fragment_book_detail

    @Inject
    lateinit var settings: DanteSettings

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var tracker: Tracker

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: BookDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)[BookDetailViewModel::class.java]

        arguments?.getLong(ARG_BOOK_ID)?.let { bookId -> viewModel.bookId = bookId }
    }

    override fun setupViews() {
        // TODO
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

    }

    override fun unbindViewModel() {
    }

    override fun onBackwardAnimation() {
    }

    companion object {

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
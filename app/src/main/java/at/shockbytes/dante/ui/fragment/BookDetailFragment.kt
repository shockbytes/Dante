package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.dialog.NotesDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.PageEditDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.RateBookDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.SimpleRequestDialogFragment
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.ui.image.ImageLoadingCallback
import at.shockbytes.dante.ui.viewmodel.BookDetailViewModel
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.dante.util.tracking.event.DanteTrackingEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_book_detail_legacy.*
import ru.bullyboo.view.CircleSeekBar
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    02.02.2019
 */
class BookDetailFragment : BaseFragment(), BackAnimatable, ImageLoadingCallback,
        Palette.PaletteAsyncListener, CircleSeekBar.Callback {

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

        arguments?.getLong(ARG_BOOK_ID)?.let { bookId -> viewModel.intializeWithBookId(bookId) }
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

    override fun onImageResourceReady(resource: Drawable?) {
        (resource as? BitmapDrawable)?.bitmap?.let { bm ->
            Palette.from(bm).generate(this)
        }
    }

    override fun onImageLoadingFailed(e: Exception?) {
        Timber.d(e)
    }

    override fun onGenerated(palette: Palette?) {

        val actionBarColor = palette?.lightMutedSwatch?.rgb
        val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette?.darkMutedSwatch?.rgb

        (activity as? TintableBackNavigableActivity)?.tintSystemBarsWithText(actionBarColor,
                actionBarTextColor, statusBarColor)
    }

    override fun onStartScrolling(startValue: Int) = Unit

    override fun onEndScrolling(endValue: Int) {
        viewModel.updateCurrentPage(endValue)
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
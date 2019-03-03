package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.ui.viewmodel.BookDetailViewModel
import at.shockbytes.dante.util.addTo
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRatingBar
import kotlinx.android.synthetic.main.fragment_rating.*
import timber.log.Timber
import javax.inject.Inject

class RateFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_rating

    @Inject
    lateinit var imageLoader: ImageLoader

    var onRateClickListener: ((Int) -> Unit)? = null

    override fun setupViews() {

        layout_rating.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        btn_rating_close.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        RxRatingBar.ratingChanges(rb_rating)
                .distinctUntilChanged()
                .subscribe({
                    val rating = it.toInt() - 1 // -1 because rating starts with 1
                    if (rating in 0..4) { // Error can somehow occur, therefore check!
                        context?.let { ctx ->
                            tv_rating_rationale.text = ctx.resources.getStringArray(R.array.ratings)[rating]
                        }
                    }
                }, { throwable -> Timber.e(throwable) })
                .addTo(compositeDisposable)

        RxView.clicks(btn_rating_rate)
                .distinctUntilChanged()
                .subscribe({
                    onRateClickListener?.invoke(rb_rating.rating.toInt())
                    fragmentManager?.popBackStack()
                }, { throwable ->
                    Timber.e(throwable)
                })
                .addTo(compositeDisposable)

        arguments?.getParcelable<BookDetailViewModel.RatingInfo>(ARG_RATE_INFO)?.let { (title, url, rating) ->

            tv_rating_header.text = getString(R.string.dialogfragment_rating_title, title)

            context?.let { ctx ->
                url?.let {
                    imageLoader.loadImageWithCornerRadius(
                        ctx,
                        url,
                        iv_rating_cover,
                        R.drawable.ic_placeholder,
                        cornerDimension = ctx.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                }
            }

            if (rating > 0) {
                rb_rating.rating = rating.toFloat()
            }
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() = Unit

    override fun unbindViewModel() = Unit

    companion object {

        private const val ARG_RATE_INFO = "arg_rate_info"

        fun newInstance(info: BookDetailViewModel.RatingInfo): RateFragment {
            return RateFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(ARG_RATE_INFO, info)
                }
            }
        }
    }
}
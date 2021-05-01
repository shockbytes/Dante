package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.FragmentRatingBinding
import at.shockbytes.dante.ui.viewmodel.BookDetailViewModel
import at.shockbytes.dante.util.addTo
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRatingBar
import timber.log.Timber
import javax.inject.Inject

class RateFragment : BaseFragment<FragmentRatingBinding>() {

    @Inject
    lateinit var imageLoader: ImageLoader

    var onRateClickListener: ((Int) -> Unit)? = null


    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentRatingBinding {
        return FragmentRatingBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {

        vb.layoutRating.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        vb.btnRatingClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        RxRatingBar.ratingChanges(vb.rbRating)
                .distinctUntilChanged()
                .subscribe({
                    val rating = it.toInt() - 1 // -1 because rating starts with 1
                    if (rating in 0..4) { // Error can somehow occur, therefore check!
                        context?.let { ctx ->
                            vb.tvRatingRationale.text = ctx.resources.getStringArray(R.array.ratings)[rating]
                        }
                    }
                }, { throwable -> Timber.e(throwable) })
                .addTo(compositeDisposable)

        RxView.clicks(vb.btnRatingRate)
                .distinctUntilChanged()
                .subscribe({
                    onRateClickListener?.invoke(vb.rbRating.rating.toInt())
                    parentFragmentManager.popBackStack()
                }, { throwable ->
                    Timber.e(throwable)
                })
                .addTo(compositeDisposable)

        arguments?.getParcelable<BookDetailViewModel.RatingInfo>(ARG_RATE_INFO)?.let { (title, url, rating) ->

            vb.tvRatingHeader.text = getString(R.string.dialogfragment_rating_title, title)

            context?.let { ctx ->
                url?.let {
                    imageLoader.loadImageWithCornerRadius(
                        ctx,
                        url,
                        vb.ivRatingCover,
                        R.drawable.ic_placeholder,
                        cornerDimension = ctx.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                }
            }

            if (rating > 0) {
                vb.rbRating.rating = rating.toFloat()
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
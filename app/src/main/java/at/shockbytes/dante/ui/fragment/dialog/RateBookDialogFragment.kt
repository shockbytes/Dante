package at.shockbytes.dante.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.util.addTo
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRatingBar
import kotterknife.bindView
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    14.01.2018
 */
class RateBookDialogFragment : InteractiveViewDialogFragment<Int>() {

    private val btnRate: Button by bindView(R.id.dialogfragment_rating_btn_rate)
    private val txtTitle: TextView by bindView(R.id.dialogfragment_rating_txt_title)
    private val ratingBar: RatingBar by bindView(R.id.dialogfragment_rating_ratingbar)
    private val txtRatings: TextView by bindView(R.id.dialogfragment_rating_txt_ratings)
    private val imgViewCover: ImageView by bindView(R.id.dialogfragment_rating_imgview_cover)

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_rating, null, false)

    private lateinit var bookTitle: String
    private var bookImageLink: String? = null
    private var previousRating: Int = 0

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookTitle = arguments?.getString(ARG_TITLE) ?: ""
        bookImageLink = arguments?.getString(ARG_IMAGE)
        previousRating = arguments?.getInt(ARG_PREV_RATING) ?: 0
    }

    override fun setupViews() {

        txtTitle.text = getString(R.string.dialogfragment_rating_title, bookTitle)
        if (!bookImageLink.isNullOrEmpty()) {
            context?.let { ctx ->
                imageLoader.loadImage(ctx, bookImageLink!!, imgViewCover, R.drawable.ic_placeholder_white)
            }
        }

        RxRatingBar.ratingChanges(ratingBar).distinctUntilChanged()
                .subscribe({
                    val rating = it.toInt() - 1 // -1 because rating starts with 1
                    if (rating in 0..4) { // Error can somehow occur, therefore check!
                        context?.let { ctx ->
                            txtRatings.text = ctx.resources.getStringArray(R.array.ratings)[rating]
                        }
                    }
                }, { throwable -> Timber.e(throwable) })
                .addTo(compositeDisposable)

        if (previousRating > 0) {
            ratingBar.rating = previousRating.toFloat()
        }

        RxView.clicks(btnRate).distinctUntilChanged()
                .subscribe {
                    applyListener?.invoke(ratingBar.rating.toInt())
                    dismiss()
                }.addTo(compositeDisposable)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    companion object {

        private const val ARG_TITLE = "arg_title"
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_PREV_RATING = "arg_prev_rating"

        fun newInstance(
            title: String,
            imageLink: String?,
            previousRating: Int
        ): RateBookDialogFragment {
            return RateBookDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_IMAGE, imageLink)
                    putInt(ARG_PREV_RATING, previousRating)
                }
            }
        }
    }
}
package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import at.shockbytes.dante.R
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxRatingBar
import com.squareup.picasso.Picasso
import kotterknife.bindView

/**
 * @author Martin Macheiner
 * Date: 14.01.2018.
 */

class RateBookDialogFragment : DialogFragment() {

    private var ratingListener: ((Int) -> Unit)? = null

    private val btnRate: Button by bindView(R.id.dialogfragment_rating_btn_rate)
    private val txtTitle: TextView by bindView(R.id.dialogfragment_rating_txt_title)
    private val ratingBar: RatingBar by bindView(R.id.dialogfragment_rating_ratingbar)
    private val txtRatings: TextView by bindView(R.id.dialogfragment_rating_txt_ratings)
    private val imgViewCover: ImageView by bindView(R.id.dialogfragment_rating_imgview_cover)

    private val rateView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_rating, null, false)

    private lateinit var bookTitle: String
    private var bookImageLink: String? = null
    private var previousRating: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookTitle = arguments.getString(ARG_TITLE)
        bookImageLink = arguments.getString(ARG_IMAGE)
        previousRating = arguments.getInt(ARG_PREV_RATING)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setView(rateView)
                .create()
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    fun setRatingListener(listener: (Int) -> Unit): RateBookDialogFragment {
        ratingListener = listener
        return this
    }

    private fun setupViews() {

        txtTitle.text = getString(R.string.dialogfragment_rating_title, bookTitle)
        if (!bookImageLink.isNullOrEmpty()) {
            Picasso.with(context).load(bookImageLink)
                    .placeholder(R.drawable.ic_placeholder_white).into(imgViewCover)
        }

        RxRatingBar.ratingChanges(ratingBar).distinctUntilChanged()
                .subscribe {
                    val rating = it.toInt() - 1 // -1 because rating starts with 1
                    txtRatings.text = context.resources.getStringArray(R.array.ratings)[rating]
                }
        if (previousRating > 0) {
            ratingBar.rating = previousRating.toFloat()
        }

        RxView.clicks(btnRate).distinctUntilChanged()
                .subscribe {
                    ratingListener?.invoke(ratingBar.rating.toInt())
                    dismiss()
                }
    }


    companion object {

        private const val ARG_TITLE = "arg_title"
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_PREV_RATING = "arg_prev_rating"

        fun newInstance(title: String, imageLink: String?,
                        previousRating: Int): RateBookDialogFragment {
            val fragment = RateBookDialogFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_IMAGE, imageLink)
            args.putInt(ARG_PREV_RATING, previousRating)
            fragment.arguments = args
            return fragment
        }

    }

}
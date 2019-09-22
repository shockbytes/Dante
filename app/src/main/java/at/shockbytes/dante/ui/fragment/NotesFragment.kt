package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.util.isPortrait
import kotlinx.android.synthetic.main.fragment_notes.*
import javax.inject.Inject

class NotesFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_notes

    @Inject
    lateinit var imageLoader: ImageLoader

    var onSavedClickListener: ((String) -> Unit)? = null

    override fun setupViews() {

        val bookTitle = arguments?.getString(ARG_TITLE) ?: ""
        val bookNotes = arguments?.getString(ARG_NOTES) ?: ""
        val bookImageLink = arguments?.getString(ARG_IMAGE)

        val lines = if (isPortrait()) 8 else 2
        et_notes.setLines(lines)
        et_notes.setText(bookNotes)
        txt_notes_header_description.text = getString(R.string.dialogfragment_notes_header, bookTitle)
        if (!bookImageLink.isNullOrEmpty()) {
            context?.let { ctx ->
                imageLoader.loadImageWithCornerRadius(
                        ctx,
                        bookImageLink,
                        iv_notes_cover,
                        R.drawable.ic_placeholder_white,
                        cornerDimension = ctx.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                )
            }
        }

        btn_notes_save.setOnClickListener {
            onSavedClickListener?.invoke(et_notes.text.toString())
            fragmentManager?.popBackStack()
        }
        btn_notes_clear.setOnClickListener {
            et_notes.setText("")
        }

        layout_notes.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        btn_notes_close.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() = Unit

    override fun unbindViewModel() = Unit

    companion object {

        private const val ARG_TITLE = "arg_title"
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_NOTES = "arg_prev_rating"

        fun newInstance(title: String, imageLink: String?, notes: String): NotesFragment {
            return NotesFragment().apply {
                this.arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_IMAGE, imageLink)
                    putString(ARG_NOTES, notes)
                }
            }
        }
    }
}
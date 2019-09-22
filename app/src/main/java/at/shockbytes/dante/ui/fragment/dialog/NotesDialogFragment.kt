package at.shockbytes.dante.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.util.isPortrait
import kotterknife.bindView
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    16.01.2018
 */
class NotesDialogFragment : InteractiveViewDialogFragment<String>() {

    private val txtHeader: TextView by bindView(R.id.dialogfragment_notes_txt_header)
    private val imgViewCover: ImageView by bindView(R.id.dialogfragment_notes_imgview)
    private val editNotes: EditText by bindView(R.id.dialogfragment_notes_edittext)
    private val btnSave: Button by bindView(R.id.dialogfragment_notes_btn_save)
    private val btnClear: Button by bindView(R.id.dialogfragment_notes_btn_clear)

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_notes, null, false)

    private lateinit var bookTitle: String
    private lateinit var bookNotes: String
    private var bookImageLink: String? = null

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookTitle = arguments?.getString(ARG_TITLE) ?: ""
        bookNotes = arguments?.getString(ARG_NOTES) ?: ""
        bookImageLink = arguments?.getString(ARG_IMAGE)
    }

    override fun setupViews() {

        val lines = if (isPortrait()) 8 else 2
        editNotes.setLines(lines)
        editNotes.setText(bookNotes)
        txtHeader.text = getString(R.string.dialogfragment_notes_header, bookTitle)
        if (!bookImageLink.isNullOrEmpty()) {
            context?.let { ctx ->
                imageLoader.loadImage(ctx, bookImageLink!!, imgViewCover, R.drawable.ic_placeholder_white)
            }
        }

        btnSave.setOnClickListener {
            applyListener?.invoke(editNotes.text.toString())
            dismiss()
        }
        btnClear.setOnClickListener {
            editNotes.setText("")
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    companion object {

        private const val ARG_TITLE = "arg_title"
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_NOTES = "arg_prev_rating"

        fun newInstance(title: String, imageLink: String?, notes: String): NotesDialogFragment {
            return NotesDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_IMAGE, imageLink)
                    putString(ARG_NOTES, notes)
                }
            }
        }
    }
}
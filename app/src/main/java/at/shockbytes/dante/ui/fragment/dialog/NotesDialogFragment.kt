package at.shockbytes.dante.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.util.DanteUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotterknife.bindView

/**
 * @author Martin Macheiner
 * Date: 16.01.2018.
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookTitle = arguments?.getString(ARG_TITLE) ?: ""
        bookNotes = arguments?.getString(ARG_NOTES) ?: ""
        bookImageLink = arguments?.getString(ARG_IMAGE)
    }

    override fun setupViews() {

        val lines = if (DanteUtils.isPortrait(context)) 8 else 2
        editNotes.setLines(lines)
        editNotes.setText(bookNotes)
        txtHeader.text = getString(R.string.dialogfragment_notes_header, bookTitle)
        if (!bookImageLink.isNullOrEmpty()) {
            context?.let { ctx ->
                Glide.with(ctx).load(bookImageLink)
                        .apply(RequestOptions().placeholder(R.drawable.ic_placeholder_white))
                        .into(imgViewCover)
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
        // Not needed
    }


    companion object {

        private const val ARG_TITLE = "arg_title"
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_NOTES = "arg_prev_rating"

        fun newInstance(title: String, imageLink: String?, notes: String): NotesDialogFragment {
            val fragment = NotesDialogFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_IMAGE, imageLink)
            args.putString(ARG_NOTES, notes)
            fragment.arguments = args
            return fragment
        }
    }

}
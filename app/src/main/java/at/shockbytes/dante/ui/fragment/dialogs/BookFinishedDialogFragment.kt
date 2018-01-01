package at.shockbytes.dante.ui.fragment.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import at.shockbytes.dante.R

/**
 * @author Martin Macheiner
 * Date: 16.09.2017.
 */
class BookFinishedDialogFragment : DialogFragment() {

    private lateinit var bookTitle: String

    private var listener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookTitle = arguments.getString(ARG_TITLE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setTitle(getString(R.string.book_finished, bookTitle))
                .setIcon(R.drawable.ic_pick_done)
                .setMessage(R.string.book_finished_move_to_done_question)
                .setPositiveButton(android.R.string.yes) { _, _ -> listener?.invoke() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }.create()
    }

    fun setOnBookMoveFinishedListener(listener: () -> Unit): BookFinishedDialogFragment {
        this.listener = listener
        return this
    }

    companion object {

        private val ARG_TITLE = "title"

        fun newInstance(bookTitle: String): BookFinishedDialogFragment {
            val fragment = BookFinishedDialogFragment()
            val args = Bundle(1)
            args.putString(ARG_TITLE, bookTitle)
            fragment.arguments = args
            return fragment
        }
    }

}

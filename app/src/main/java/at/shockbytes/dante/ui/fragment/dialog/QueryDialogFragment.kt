package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import at.shockbytes.dante.R
import kotterknife.bindView

/**
 * @author Martin Macheiner
 * Date: 30.08.2016.
 */
class QueryDialogFragment : DialogFragment() {

    private val editQuery: EditText by bindView(R.id.dialogfragment_isbn_edit)

    private var queryListener: ((String) -> Unit)? = null

    private val queryView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_enter_query, null, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(queryView)
                .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
                .setPositiveButton(android.R.string.search_go) { _, _ ->
                    // Remove blanks with + so query works also for titles
                    val query = editQuery.text.toString().replace(' ', '+')
                    queryListener?.invoke(query)
                }
                .create()
                .also { it.requestWindowFeature(Window.FEATURE_NO_TITLE) }
    }

    fun setOnQueryEnteredListener(listener: (String) -> Unit): QueryDialogFragment {
        this.queryListener = listener
        return this
    }

    companion object {

        fun newInstance(): QueryDialogFragment {
            val fragment = QueryDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}

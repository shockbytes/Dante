package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import at.shockbytes.dante.R
import kotterknife.bindView

/**
 * @author Martin Macheiner
 * Date: 15.01.2018.
 */

class PageEditDialogFragment : androidx.fragment.app.DialogFragment() {

    private val editPages: EditText by bindView(R.id.dialogfragment_paging_edit_pages)
    private val editCurrentPages: EditText by bindView(R.id.dialogfragment_paging_edit_current_page)

    private var pageEditListener: ((current: Int, pages: Int) -> Unit)? = null

    private var currentPage: Int = 0
    private var pages: Int = 0

    private val pageView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_paging, null, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pages = arguments?.getInt(ARG_PAGES) ?: 0
        currentPage = arguments?.getInt(ARG_CURRENT_PAGE) ?: 0
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(pageView)
                .setPositiveButton(R.string.apply) { _, _ ->

                    if (validateInput()) {
                        val current = editCurrentPages.text.toString().toInt()
                        val pages = editPages.text.toString().toInt()
                        pageEditListener?.invoke(current, pages)
                    } else {
                        Toast.makeText(context, getString(R.string.dialogfragment_paging_error),
                                Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .create()
                .also { it.requestWindowFeature(Window.FEATURE_NO_TITLE) }
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    fun setOnPageEditedListener(listener: (current: Int, pages: Int) -> Unit): PageEditDialogFragment {
        pageEditListener = listener
        return this
    }

    private fun validateInput(): Boolean {

        val current = editCurrentPages.text.toString().toIntOrNull()
        val pages = editPages.text.toString().toIntOrNull()

        return if (current == null || pages == null) {
            false
        } else {
            pages >= current
        }
    }

    private fun setupViews() {
        editCurrentPages.setText(currentPage.toString())
        editPages.setText(pages.toString())
    }

    companion object {

        private const val ARG_CURRENT_PAGE = "arg_current_page"
        private const val ARG_PAGES = "arg_pages"

        fun newInstance(current: Int, pages: Int): PageEditDialogFragment {
            val fragment = PageEditDialogFragment()
            val args = Bundle()
            args.putInt(ARG_CURRENT_PAGE, current)
            args.putInt(ARG_PAGES, pages)
            fragment.arguments = args
            return fragment
        }
    }
}
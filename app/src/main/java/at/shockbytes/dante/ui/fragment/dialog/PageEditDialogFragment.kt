package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.shockbytes.dante.R
import kotterknifex.bindView

/**
 * @author Martin Macheiner
 * Date: 15.01.2018.
 */

class PageEditDialogFragment : DialogFragment() {

    private val separator: View by bindView(R.id.dialogfragment_paging_separator)
    private val editPages: EditText by bindView(R.id.dialogfragment_paging_edit_pages)
    private val editCurrentPages: EditText by bindView(R.id.dialogfragment_paging_edit_current_page)

    private var pageEditListener: ((current: Int, pages: Int) -> Unit)? = null

    private var currentPage: Int = 0
    private var pages: Int = 0
    private var showCurrentPage: Boolean = true

    private val pageView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_paging, null, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pages = arguments?.getInt(ARG_PAGES) ?: 0
        currentPage = arguments?.getInt(ARG_CURRENT_PAGE) ?: 0
        showCurrentPage = arguments?.getBoolean(ARG_PAGE_TRACKING) ?: true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(pageView)
                .setTitle(getString(R.string.dialogfragment_paging_title))
                .setIcon(R.drawable.ic_pages_colored)
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

        if (showCurrentPage) {
            editCurrentPages.setText(currentPage.toString())
        } else {
            editCurrentPages.visibility = View.GONE
            separator.visibility = View.GONE
        }
        editPages.setText(pages.toString())
    }

    companion object {

        private const val ARG_PAGE_TRACKING = "arg_page_tracking"
        private const val ARG_CURRENT_PAGE = "arg_current_page"
        private const val ARG_PAGES = "arg_pages"

        fun newInstance(current: Int, pages: Int,
                        showCurrentPage: Boolean): PageEditDialogFragment {
            val fragment = PageEditDialogFragment()
            val args = Bundle()
            args.putInt(ARG_CURRENT_PAGE, current)
            args.putInt(ARG_PAGES, pages)
            args.putBoolean(ARG_PAGE_TRACKING, showCurrentPage)
            fragment.arguments = args
            return fragment
        }

    }

}
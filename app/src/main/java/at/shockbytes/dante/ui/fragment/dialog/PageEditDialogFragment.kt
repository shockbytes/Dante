package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import at.shockbytes.dante.R
import kotterknife.bindView

/**
 * @author Martin Macheiner
 * Date: 15.01.2018.
 */

class PageEditDialogFragment : DialogFragment() {

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
        currentPage = arguments.getInt(ARG_CURRENT_PAGE)
        pages = arguments.getInt(ARG_PAGES)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setView(pageView)
                .setTitle(getString(R.string.dialogfragment_paging_title))
                .setIcon(R.drawable.ic_pages_colored)
                .setPositiveButton(R.string.apply) { _, _ ->

                    val current = editCurrentPages.text.toString().toInt()
                    val pages = editPages.text.toString().toInt()
                    if (pages > current) {
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

    private fun setupViews() {
        editPages.setText(pages.toString())
        editCurrentPages.setText(currentPage.toString())
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
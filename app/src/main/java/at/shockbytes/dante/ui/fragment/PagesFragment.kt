package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import kotlinx.android.synthetic.main.fragment_pages.*

class PagesFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_pages

    var onPageEditedListener: ((current: Int, pages: Int) -> Unit)? = null

    override fun setupViews() {

        arguments?.getInt(ARG_CURRENT)?.let { current ->
            et_pages_current_page.setText(current.toString())
        }
        arguments?.getInt(ARG_PAGES)?.let { pages ->
            et_pages_pages.setText(pages.toString())
        }

        img_btn_fragment_pages_inc.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            increasePageNumber(v)
        }

        img_btn_fragment_pages_dec.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            decreasePageNumber(v)
        }

        btn_pages_save.setOnClickListener {

            val current = et_pages_current_page.text?.toString()?.toIntOrNull()
            val pages = et_pages_pages.text?.toString()?.toIntOrNull()

            if (validateInput()) {
                if (current != null && pages != null) {
                    onPageEditedListener?.invoke(current, pages)
                    fragmentManager?.popBackStack()
                }
            } else {
                Toast.makeText(context, getString(R.string.dialogfragment_paging_error),
                        Toast.LENGTH_SHORT).show()
            }
        }

        layout_pages.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        btn_pages_close.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }

    private fun increasePageNumber(v: View) {

        val current = et_pages_current_page.text.toString().toIntOrNull() ?: 0
        val pages = et_pages_pages.text.toString().toIntOrNull() ?: 0

        if (current < pages) {
            et_pages_current_page.setText(current.inc().toString())
        } else {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    private fun decreasePageNumber(v: View) {
        val current = et_pages_current_page.text.toString().toIntOrNull() ?: 0

        if (current > 0) {
            et_pages_current_page.setText(current.dec().toString())
        } else {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    override fun bindViewModel() = Unit

    override fun unbindViewModel() = Unit

    private fun validateInput(): Boolean {

        val current = et_pages_current_page.text.toString().toIntOrNull()
        val pages = et_pages_pages.text.toString().toIntOrNull()

        return if (current == null || pages == null) {
            false
        } else {
            pages >= current
        }
    }

    companion object {

        private const val ARG_CURRENT = "arg_current"
        private const val ARG_PAGES = "arg_pages"

        fun newInstance(current: Int, pages: Int): PagesFragment {
            return PagesFragment().apply {
                this.arguments = Bundle().apply {
                    putInt(ARG_CURRENT, current)
                    putInt(ARG_PAGES, pages)
                }
            }
        }
    }
}
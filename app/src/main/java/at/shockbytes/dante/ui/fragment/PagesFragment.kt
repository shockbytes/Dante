package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.FragmentPagesBinding
import at.shockbytes.dante.injection.AppComponent

class PagesFragment : BaseFragment<FragmentPagesBinding>() {

    var onPageEditedListener: ((current: Int, pages: Int) -> Unit)? = null

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentPagesBinding {
        return FragmentPagesBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {

        arguments?.getInt(ARG_CURRENT)?.let { current ->
            vb.etPagesCurrentPage.setText(current.toString())
        }
        arguments?.getInt(ARG_PAGES)?.let { pages ->
            vb.etPagesPages.setText(pages.toString())
        }

        vb.imgBtnFragmentPagesInc.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            increasePageNumber(v)
        }

        vb.imgBtnFragmentPagesDec.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            decreasePageNumber(v)
        }

        vb.btnPagesSave.setOnClickListener {

            val current = getCurrentPage()
            val pages = getPages()

            if (validateInput()) {
                if (current != null && pages != null) {
                    onPageEditedListener?.invoke(current, pages)
                    closeFragment()
                }
            } else {
                Toast.makeText(context, getString(R.string.dialogfragment_paging_error),
                        Toast.LENGTH_SHORT).show()
            }
        }

        vb.layoutPages.setOnClickListener {
            closeFragment()
        }

        vb.btnPagesClose.setOnClickListener {
            closeFragment()
        }
    }

    private fun closeFragment() {
        parentFragmentManager.popBackStack()
    }

    private fun increasePageNumber(v: View) {

        val current = getCurrentPageOrElse(default = 0)
        val pages = getPagesOrElse(default = 0)

        if (current < pages) {
            vb.etPagesCurrentPage.setText(current.inc().toString())
        } else {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    private fun decreasePageNumber(v: View) {
        val current = getCurrentPageOrElse(default = 0)

        if (current > 0) {
            vb.etPagesCurrentPage.setText(current.dec().toString())
        } else {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    override fun bindViewModel() = Unit

    override fun unbindViewModel() = Unit

    private fun validateInput(): Boolean {

        val current = getCurrentPage()
        val pages = getPages()

        return if (current == null || pages == null) {
            false
        } else {
            pages >= current
        }
    }

    private fun getCurrentPage(): Int? {
        return vb.etPagesCurrentPage.text.toString().toIntOrNull()
    }

    private fun getPages(): Int? {
        return vb.etPagesPages.text.toString().toIntOrNull()
    }

    private fun getCurrentPageOrElse(default: Int): Int {
        return vb.etPagesCurrentPage.text.toString().toIntOrNull() ?: default
    }

    private fun getPagesOrElse(default: Int): Int {
        return vb.etPagesPages.text.toString().toIntOrNull() ?: default
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
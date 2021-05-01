package at.shockbytes.dante.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.SearchViewBinding
import at.shockbytes.dante.util.layoutInflater
import at.shockbytes.dante.util.setVisible

class SearchView(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    private val vb = SearchViewBinding.inflate(context.layoutInflater(), this, true)

    var currentQuery: CharSequence = ""
        private set

    var queryListener: ((CharSequence) -> Unit)? = null

    var homeActionClickListener: (() -> Unit)? = null

    init {
        showProgress(false)
        clearQuery()

        setupQueryView()
        setupHomeView()
        setupCancelView()
    }

    fun setSearchFocused(isFocused: Boolean) {
        vb.searchViewEditQuery.isFocusable = isFocused

        if (isFocused) {
            vb.searchViewEditQuery.requestFocus()
        }
    }

    fun showProgress(showProgress: Boolean) {

        if (showProgress) {

            vb.searchViewPb.visibility = View.VISIBLE
            vb.searchViewPb.animate()
                .translationX(0f)
                .alpha(1f)
                .start()

            vb.searchViewImgbtnHome.animate()
                .scaleX(0.4f)
                .scaleY(0.4f)
                .alpha(0f)
                .withEndAction { vb.searchViewImgbtnHome.visibility = View.INVISIBLE }
                .start()
        } else {

            vb.searchViewPb.animate()
                .alpha(0f)
                .withEndAction {
                    vb.searchViewPb.apply {
                        visibility = View.INVISIBLE
                        translationX = -80f
                    }
                }
                .start()

            vb.searchViewImgbtnHome.visibility = View.VISIBLE
            vb.searchViewImgbtnHome.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .start()
        }
    }

    fun clearQuery() {
        currentQuery = ""
        vb.searchViewImgbtnCancel.setVisible(false)
    }

    // -------------------------------------------------------------------

    private fun setupQueryView() {

        vb.searchViewEditQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {

                    currentQuery = it
                    queryListener?.invoke(currentQuery)

                    updateCancelButton()
                }
            }
        })
    }

    private fun setupHomeView() {

        vb.searchViewPb.apply {
            alpha = 0f
            translationX = -80f
            visibility = View.INVISIBLE
        }

        vb.searchViewImgbtnHome.setOnClickListener {
            homeActionClickListener?.invoke()
        }
    }

    private fun setupCancelView() {

        vb.searchViewImgbtnCancel.apply {
            alpha = 0f
            translationX = 30f
            visibility = View.INVISIBLE
            setOnClickListener {
                vb.searchViewEditQuery.setText("")
            }
        }
    }

    private fun updateCancelButton() {

        if (currentQuery.isNotEmpty()) {

            vb.searchViewImgbtnCancel.apply {
                visibility = View.VISIBLE
                animate()
                    .translationX(0f)
                    .alpha(1f)
                    .start()
            }

        } else {
            vb.searchViewImgbtnCancel.animate()
                .translationX(30f)
                .alpha(0f)
                .withEndAction { vb.searchViewImgbtnCancel.visibility = View.INVISIBLE }
                .start()
        }
    }
}
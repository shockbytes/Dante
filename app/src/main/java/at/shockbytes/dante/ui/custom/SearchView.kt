package at.shockbytes.dante.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.util.setVisible
import kotlinx.android.synthetic.main.search_view.view.*

class SearchView(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    var currentQuery: CharSequence = ""
        private set

    var queryListener: ((CharSequence) -> Unit)? = null

    var homeActionClickListener: (() -> Unit)? = null


    init {
        inflate(context, R.layout.search_view, this)
        showProgress(false)
        clearQuery()

        setupQueryView()
        setupHomeView()
        setupCancelView()
    }

    fun setSearchFocused(isFocused: Boolean) {
        search_view_edit_query.isFocusable = isFocused

        if (isFocused) {
            search_view_edit_query.requestFocus()
        }
    }

    fun showProgress(showProgress: Boolean) {

        // TODO smooth animation between both
        if (showProgress) {
            search_view_pb.setVisible(true)
            search_view_imgbtn_home.visibility = View.INVISIBLE
        } else {
            search_view_pb.setVisible(false)
            search_view_imgbtn_home.visibility = View.VISIBLE
        }
    }

    fun clearQuery() {
        currentQuery = ""
        search_view_imgbtn_cancel.setVisible(false)
    }

    // -------------------------------------------------------------------

    private fun setupQueryView() {

        search_view_edit_query.addTextChangedListener(object : TextWatcher {
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
        search_view_imgbtn_home.setOnClickListener {
            homeActionClickListener?.invoke()
        }
    }

    private fun setupCancelView() {
        search_view_imgbtn_cancel.setOnClickListener {
            search_view_edit_query.setText("")
        }
    }

    private fun updateCancelButton() {

        // TODO Smooth animation
        if (currentQuery.isNotEmpty()) {
            search_view_imgbtn_cancel.setVisible(true)
        } else {
            search_view_imgbtn_cancel.setVisible(false)
        }
    }

}
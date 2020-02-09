package at.shockbytes.dante.ui.custom

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.util.setVisible
import kotlinx.android.synthetic.main.stats_header_view.view.*

class StatsHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    init {
        inflate(context, R.layout.stats_header_view, this)

        context.theme.obtainStyledAttributes(attrs, R.styleable.StatsHeaderView, defStyle, 0).run {
            initializeWithAttributes(this)
            this.recycle()
        }
    }

    private fun initializeWithAttributes(attributes: TypedArray) {

        val titleResId = attributes.getResourceId(R.styleable.StatsHeaderView_header_title, 0)

        if (titleResId != 0) {
            setHeaderTitle(context.getString(titleResId))
        }

        val showDivider = attributes.getBoolean(R.styleable.StatsHeaderView_show_divider, true)
        showDivider(showDivider)
    }

    fun setHeaderTitle(title: CharSequence) {
        tv_stats_header_view.text = title
    }

    fun showDivider(showDivider: Boolean) {
        view_stats_header_view_1.setVisible(showDivider)
        view_stats_header_view_2.setVisible(showDivider)
    }
}
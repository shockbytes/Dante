package at.shockbytes.dante.ui.custom

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.StatsHeaderViewBinding
import at.shockbytes.dante.util.layoutInflater
import at.shockbytes.dante.util.setVisible

class StatsHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val vb = StatsHeaderViewBinding.inflate(context.layoutInflater(), this, true)

    init {

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
        vb.tvStatsHeaderView.text = title
    }

    fun setHeaderTitleResource(@StringRes titleRes: Int) {
        vb.tvStatsHeaderView.setText(titleRes)
    }

    fun showDivider(showDivider: Boolean) {
        vb.viewStatsHeaderView1.setVisible(showDivider)
        vb.viewStatsHeaderView2.setVisible(showDivider)
    }
}
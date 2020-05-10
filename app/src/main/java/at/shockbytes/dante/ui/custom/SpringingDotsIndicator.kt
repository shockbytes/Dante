package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import at.shockbytes.dante.R
import kotlinx.android.synthetic.main.springing_dots_indicator.view.*

class SpringingDotsIndicator(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        gravity = Gravity.CENTER
        orientation = HORIZONTAL
    }

    fun setupWithViewPager2(viewPager2: ViewPager2) {

        val itemCount = viewPager2.adapter?.itemCount
            ?: throw IllegalStateException("ViewPager must provide an adapter!")

        addDotViews(itemCount)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectDot(position)
            }
        })

    }

    private fun addDotViews(itemCount: Int) {
        (0 until itemCount)
            .map { idx ->
                SpringDotsView(context).apply {
                    select(isDotNowSelected = idx == 0)
                }
            }
            .forEach { view ->
                addView(view)
            }
    }

    private fun selectDot(position: Int) {
        (0 until childCount)
            .forEach { idx ->
                (getChildAt(idx) as SpringDotsView).select(isDotNowSelected = idx == position)
            }
    }

    private class SpringDotsView(context: Context) : FrameLayout(context) {

        private enum class Transition {
            NONE,
            TO_SELECTED_STATE,
            TO_UNSELECTED_STATE
        }

        private var isDotSelected: Boolean = false

        init {
            inflate(context, R.layout.springing_dots_indicator, this)
        }

        fun select(isDotNowSelected: Boolean) {

            when (computeTransition(isDotNowSelected)) {
                Transition.NONE -> Unit // Do nothing
                Transition.TO_SELECTED_STATE -> {
                    // TODO Play spring animation
                    root_springing_dots_indicator.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                }
                Transition.TO_UNSELECTED_STATE -> {
                    // TODO Play spring animation
                    root_springing_dots_indicator.setCardBackgroundColor(ContextCompat.getColor(context, R.color.recycler_divider))
                }
            }

            isDotSelected = isDotNowSelected

        }

        private fun computeTransition(isDotSelectedNow: Boolean): Transition {

            return when {
                isDotSelectedNow && !isDotSelected -> Transition.TO_SELECTED_STATE
                !isDotSelectedNow && isDotSelected -> Transition.TO_UNSELECTED_STATE
                else -> Transition.NONE
            }
        }
    }
}
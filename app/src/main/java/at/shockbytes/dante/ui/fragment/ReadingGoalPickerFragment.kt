package at.shockbytes.dante.ui.fragment

import androidx.annotation.StringRes
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.ReadingGoal
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.arguments.argumentNullable
import kotlinx.android.synthetic.main.fragment_reading_goal_picker.*

class ReadingGoalPickerFragment : BaseFragment() {

    interface OnReadingGoalPickedListener {

        fun onGoalPicked(goal: Int)

        fun onDelete()
    }

    private enum class GoalType(
            @StringRes val title: Int,
            @StringRes val labelTemplate: Int
    ) {
        PAGES(R.string.reading_goal_pages, R.string.pages_formatted),
        BOOKS(R.string.reading_goal_books, R.string.books_formatted)
    }

    override val layoutId: Int = R.layout.fragment_reading_goal_picker

    private var type: GoalType by argument()
    private var initialValue: Int? by argumentNullable()

    private var onReadingGoalPickedListener: OnReadingGoalPickedListener? = null

    override fun setupViews() {
        setupLottieAnimation()
        setupCloseListeners()
        setupSlider()
        setupCallbackListeners()
        setupType()

        initialValue?.toFloat()?.let(slider_reading_goal::setValue)
    }

    private fun setupCallbackListeners() {
        btn_reading_goal_delete.setOnClickListener {
            onReadingGoalPickedListener?.onDelete()
            closeFragment()
        }

        btn_reading_goal_apply.setOnClickListener {
            onReadingGoalPickedListener?.onGoalPicked(slider_reading_goal.value.toInt())
            closeFragment()
        }
    }

    private fun setupLottieAnimation() {
        lav_reading_goal.apply {
            setMinFrame(MIN_FRAME)
            setMaxFrame(MAX_FRAME)
        }
    }

    private fun setupCloseListeners() {
        btn_reading_goal_close.setOnClickListener {
            closeFragment()
        }
        layout_reading_goal_picker.setOnClickListener {
            closeFragment()
        }
    }

    private fun closeFragment() {
        parentFragmentManager.popBackStack()
    }

    private fun setupSlider() {
        slider_reading_goal.addOnChangeListener { slider, value, _ ->
            computeLottieFrame(slider.valueFrom, slider.valueTo, value)
            updateLevel(slider.valueFrom, slider.valueTo, value)
            updateGoalLabel(value.toInt())
        }
    }

    private fun computeLottieFrame(minValue: Float, maxValue: Float, value: Float) {

        val valueP = ((100f / (maxValue - minValue)) * value).div(100)
        val frame = ((MAX_FRAME - MIN_FRAME) * valueP).toInt() + MIN_FRAME

        lav_reading_goal.frame = frame
    }

    private fun updateLevel(min: Float, max: Float, value: Float) {

        val diff = max - min

        val levelRes = when {
            value > diff.times(0.8) -> R.string.reading_goal_level_3
            value > diff.times(0.6) -> R.string.reading_goal_level_2
            value > diff.times(0.4) -> R.string.reading_goal_level_1
            else -> R.string.reading_goal_level_0
        }

        tv_reading_goal_level.setText(levelRes)
    }

    private fun setupType() {
        tv_fragment_reading_goal_header.setText(type.title)
    }

    private fun updateGoalLabel(value: Int) {
        tv_reading_goal_label.text = getString(type.labelTemplate, value)
    }

    fun setOnReadingGoalPickedListener(listener: OnReadingGoalPickedListener): ReadingGoalPickerFragment {
        return apply {
            onReadingGoalPickedListener = listener
        }
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit
    override fun bindViewModel() = Unit
    override fun unbindViewModel() = Unit

    companion object {

        private const val MIN_FRAME = 18
        private const val MAX_FRAME = 28

        fun newPagesInstance(initialValue: Int? = null): ReadingGoalPickerFragment {
            return newInstance(initialValue, type = GoalType.PAGES)
        }

        fun newBooksInstance(initialValue: Int? = null): ReadingGoalPickerFragment {
            return newInstance(initialValue, type = GoalType.BOOKS)
        }

        private fun newInstance(
                initialValue: Int? = null,
                type: GoalType
        ): ReadingGoalPickerFragment {
            return ReadingGoalPickerFragment().apply {
                this.initialValue = initialValue
                this.type = type
            }
        }
    }
}
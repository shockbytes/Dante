package at.shockbytes.dante.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.FragmentReadingGoalPickerBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.stats.model.ReadingGoalType
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.arguments.argumentNullable

class ReadingGoalPickerFragment : BaseFragment<FragmentReadingGoalPickerBinding>() {

    interface OnReadingGoalPickedListener {

        fun onGoalPicked(goal: Int, goalType: ReadingGoalType)

        fun onDelete(goalType: ReadingGoalType)
    }

    private var type: ReadingGoalType by argument()
    private var initialValue: Int? by argumentNullable()

    private var onReadingGoalPickedListener: OnReadingGoalPickedListener? = null

    override fun setupViews() {
        setupLottieAnimation()
        setupCloseListeners()
        setupSlider()
        setupCallbackListeners()
        setupType()

        initialValue?.toFloat()?.let(vb.sliderReadingGoal::setValue)
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentReadingGoalPickerBinding {
        return FragmentReadingGoalPickerBinding.inflate(inflater, root, attachToRoot)
    }

    private fun setupCallbackListeners() {
        vb.btnReadingGoalDelete.setOnClickListener {
            onReadingGoalPickedListener?.onDelete(type)
            closeFragment()
        }

        vb.btnReadingGoalApply.setOnClickListener {
            onReadingGoalPickedListener?.onGoalPicked(vb.sliderReadingGoal.value.toInt(), type)
            closeFragment()
        }
    }

    private fun setupLottieAnimation() {
        vb.lavReadingGoal.apply {
            setMinFrame(MIN_FRAME)
            setMaxFrame(MAX_FRAME)
        }
    }

    private fun setupCloseListeners() {
        vb.btnReadingGoalClose.setOnClickListener {
            closeFragment()
        }
        vb.layoutReadingGoalPicker.setOnClickListener {
            closeFragment()
        }
    }

    private fun closeFragment() {
        parentFragmentManager.popBackStack()
    }

    private fun setupSlider() {
        vb.sliderReadingGoal.apply {

            valueTo = type.sliderValueTo
            valueFrom = type.sliderValueFrom
            stepSize = type.sliderStepSize

            addOnChangeListener { slider, value, _ ->
                computeLottieFrame(slider.valueFrom, slider.valueTo, value)
                updateLevel(slider.valueFrom, slider.valueTo, value)
                updateGoalLabel(value.toInt())
            }
        }
    }

    private fun computeLottieFrame(minValue: Float, maxValue: Float, value: Float) {

        val valueP = ((100f / (maxValue - minValue)) * value).div(100)
        val frame = ((MAX_FRAME - MIN_FRAME) * valueP).toInt() + MIN_FRAME

        vb.lavReadingGoal.frame = frame
    }

    private fun updateLevel(min: Float, max: Float, value: Float) {

        val diff = max - min

        val levelRes = when {
            value > diff.times(0.8) -> R.string.reading_goal_level_3
            value > diff.times(0.6) -> R.string.reading_goal_level_2
            value > diff.times(0.4) -> R.string.reading_goal_level_1
            else -> R.string.reading_goal_level_0
        }

        vb.tvReadingGoalLevel.setText(levelRes)
    }

    private fun setupType() {
        vb.tvFragmentReadingGoalHeader.setText(type.title)
    }

    private fun updateGoalLabel(value: Int) {
        vb.tvReadingGoalLabel.text = getString(type.labelTemplate, value, getString(R.string.month))
    }

    fun setOnReadingGoalPickedListener(
        listener: OnReadingGoalPickedListener
    ): ReadingGoalPickerFragment {
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

        fun newInstance(
            initialValue: Int? = null,
            type: ReadingGoalType
        ): ReadingGoalPickerFragment {
            return ReadingGoalPickerFragment().apply {
                this.initialValue = initialValue
                this.type = type
            }
        }
    }
}
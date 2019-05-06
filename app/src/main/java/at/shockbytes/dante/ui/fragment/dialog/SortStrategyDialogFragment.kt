package at.shockbytes.dante.ui.fragment.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.sort.SortStrategy
import kotterknife.bindView
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    14.06.2018
 */
class SortStrategyDialogFragment : InteractiveViewDialogFragment<Unit>() {

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_sort, null, false)

    @Inject
    lateinit var settings: DanteSettings

    private val posIdMap = mapOf(Pair(0, R.id.radioBtnDialogFragmentSortDefault),
            Pair(1, R.id.radioBtnDialogFragmentSortAuthor),
            Pair(2, R.id.radioBtnDialogFragmentSortTitle),
            Pair(3, R.id.radioBtnDialogFragmentSortProgress),
            Pair(4, R.id.radioBtnDialogFragmentSortPages))

    private val strategyHint = listOf(
            R.string.sort_strategy_default_hint,
            R.string.sort_strategy_author_hint,
            R.string.sort_strategy_title_hint,
            R.string.sort_strategy_progress_hint,
            R.string.sort_strategy_pages_hint)

    private val radioGroupDialogFragmentSort: RadioGroup by bindView(R.id.radioGroupDialogFragmentSort)
    private val btnDialogFragmentSortApply: Button by bindView(R.id.btnDialogFragmentSortApply)
    private val txtDialogFragmentSortHint: TextView by bindView(R.id.txtDialogFragmentSortHint)

    override fun setupViews() {

        val selectedId = positionToId(settings.sortStrategy.ordinal)
        radioGroupDialogFragmentSort.check(selectedId)
        txtDialogFragmentSortHint.text = hintForId(selectedId)

        radioGroupDialogFragmentSort.setOnCheckedChangeListener { _, checkedId ->
            txtDialogFragmentSortHint.text = hintForId(checkedId)
        }

        btnDialogFragmentSortApply.setOnClickListener {
            val pos = idToPosition(radioGroupDialogFragmentSort.checkedRadioButtonId)
            settings.sortStrategy = SortStrategy.values()[pos]
            applyListener?.invoke(Unit)
            dismiss()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    private fun idToPosition(id: Int): Int = posIdMap.entries.find { it.value == id }?.key ?: 0

    private fun positionToId(pos: Int): Int = posIdMap.getValue(pos)

    private fun hintForId(id: Int) = getString(strategyHint[idToPosition(id)])

    companion object {

        fun newInstance(): SortStrategyDialogFragment {
            return SortStrategyDialogFragment()
        }
    }
}
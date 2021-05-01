package at.shockbytes.dante.ui.fragment.dialog

import android.view.LayoutInflater
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.DialogfragmentSortBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.sort.SortStrategy
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    14.06.2018
 */
class SortStrategyDialogFragment : InteractiveViewDialogFragment<Unit, DialogfragmentSortBinding>() {

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_sort, null, false)

    override val vb: DialogfragmentSortBinding
        get() = DialogfragmentSortBinding.bind(containerView)

    @Inject
    lateinit var settings: DanteSettings

    private val posIdMap = mapOf(
        Pair(0, R.id.radioBtnDialogFragmentSortDefault),
        Pair(1, R.id.radioBtnDialogFragmentSortAuthor),
        Pair(2, R.id.radioBtnDialogFragmentSortTitle),
        Pair(3, R.id.radioBtnDialogFragmentSortProgress),
        Pair(4, R.id.radioBtnDialogFragmentSortPages),
        Pair(5, R.id.radioBtnDialogFragmentSortLabels)
    )

    private val strategyHint = listOf(
        R.string.sort_strategy_default_hint,
        R.string.sort_strategy_author_hint,
        R.string.sort_strategy_title_hint,
        R.string.sort_strategy_progress_hint,
        R.string.sort_strategy_pages_hint,
        R.string.sort_strategy_labels_hint
    )

    // private val radioGroupDialogFragmentSort: RadioGroup by bindView(R.id.radioGroupDialogFragmentSort)
    // private val btnDialogFragmentSortApply: MaterialButton by bindView(R.id.btnDialogFragmentSortApply)
    // private val txtDialogFragmentSortHint: TextView by bindView(R.id.txtDialogFragmentSortHint)

    override fun setupViews() {

        val selectedId = positionToId(settings.sortStrategy.ordinal)
        vb.radioGroupDialogFragmentSort.check(selectedId)
        vb.txtDialogFragmentSortHint.text = hintForId(selectedId)

        vb.radioGroupDialogFragmentSort.setOnCheckedChangeListener { _, checkedId ->
            vb.txtDialogFragmentSortHint.text = hintForId(checkedId)
        }

        vb.btnDialogFragmentSortApply.setOnClickListener {
            val pos = idToPosition(vb.radioGroupDialogFragmentSort.checkedRadioButtonId)
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
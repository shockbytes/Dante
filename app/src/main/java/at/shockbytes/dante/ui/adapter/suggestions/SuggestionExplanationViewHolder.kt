package at.shockbytes.dante.ui.adapter.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_generic_explanation.*

class SuggestionExplanationViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<SuggestionsAdapterItem>(containerView), LayoutContainer {

    override fun bindToView(content: SuggestionsAdapterItem, position: Int) {
        // TODO

        iv_item_generic_explanation_dismiss.setOnClickListener {
            // TODO Callback
        }

        tv_item_generic_explanation.text = "Describe"

        btn_item_generic_explanation.setVisible(false, invisibilityState = View.INVISIBLE)

        iv_item_generic_explanation_decoration_start.setImageResource(R.drawable.ic_suggestions)
        iv_item_generic_explanation_decoration_end.setImageResource(R.drawable.ic_suggestions)
    }

    companion object {

        fun forParent(parent: ViewGroup): SuggestionExplanationViewHolder {
            return SuggestionExplanationViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_generic_explanation, parent, false)
            )
        }
    }
}

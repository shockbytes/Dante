package at.shockbytes.dante.ui.adapter.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class SuggestionExplanationViewHolder(
    override val containerView: View
) : BaseAdapter.ViewHolder<SuggestionsAdapterItem>(containerView), LayoutContainer {

    override fun bindToView(content: SuggestionsAdapterItem, position: Int) {
        // TODO
    }

    companion object {

        fun forParent(parent: ViewGroup): SuggestionExplanationViewHolder {
            return SuggestionExplanationViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion_explanation, parent, false)
            )
        }
    }
}

package at.shockbytes.dante.ui.adapter.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_generic_explanation.*

class SuggestionHintViewHolder(
    override val containerView: View,
    private val onSuggestionExplanationClickedListener: OnSuggestionExplanationClickedListener
) : BaseAdapter.ViewHolder<SuggestionsAdapterItem>(containerView), LayoutContainer {

    override fun bindToView(content: SuggestionsAdapterItem, position: Int) {

        iv_item_generic_explanation_dismiss.setOnClickListener {
            onSuggestionExplanationClickedListener.onDismissClicked()
        }

        tv_item_generic_explanation.setText(R.string.suggestions_hint)

        iv_item_generic_explanation_decoration_start.setImageResource(R.drawable.ic_suggestions)
        iv_item_generic_explanation_decoration_end.setImageResource(R.drawable.ic_suggestions)
    }

    companion object {

        fun forParent(
            parent: ViewGroup,
            onSuggestionExplanationClickedListener: OnSuggestionExplanationClickedListener
        ): SuggestionHintViewHolder {
            return SuggestionHintViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_generic_explanation, parent, false),
                onSuggestionExplanationClickedListener
            )
        }
    }
}

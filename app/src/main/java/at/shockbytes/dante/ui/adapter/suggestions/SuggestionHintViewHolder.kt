package at.shockbytes.dante.ui.adapter.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ItemGenericExplanationBinding
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class SuggestionHintViewHolder(
    override val containerView: View,
    private val onSuggestionExplanationClickedListener: OnSuggestionExplanationClickedListener
) : BaseAdapter.ViewHolder<SuggestionsAdapterItem>(containerView), LayoutContainer {

    private val vb = ItemGenericExplanationBinding.bind(containerView)

    override fun bindToView(content: SuggestionsAdapterItem, position: Int) {

        vb.ivItemGenericExplanationDismiss.setOnClickListener {
            onSuggestionExplanationClickedListener.onDismissClicked()
        }

        vb.tvItemGenericExplanation.setText(R.string.suggestions_hint)

        vb.ivItemGenericExplanationDecorationStart.setImageResource(R.drawable.ic_suggestions)
        vb.ivItemGenericExplanationDecorationEnd.setImageResource(R.drawable.ic_suggestions)
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

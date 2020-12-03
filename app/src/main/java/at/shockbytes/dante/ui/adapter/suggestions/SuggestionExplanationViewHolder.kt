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
    override val containerView: View,
    private val onSuggestionExplanationClickedListener: OnSuggestionExplanationClickedListener
) : BaseAdapter.ViewHolder<SuggestionsAdapterItem>(containerView), LayoutContainer {

    override fun bindToView(content: SuggestionsAdapterItem, position: Int) {

        val userWantsToSuggest = (content as SuggestionsAdapterItem.Explanation).wantsToSuggest
        val btnTextRes = if (userWantsToSuggest) {
            R.string.suggestions_explanation_want_to_suggest_clicked
        } else R.string.suggestions_explanation_want_to_suggest

        iv_item_generic_explanation_dismiss.setOnClickListener {
            onSuggestionExplanationClickedListener.onDismissClicked()
        }

        tv_item_generic_explanation.setText(R.string.suggestions_explanation)

        btn_item_generic_explanation.apply {
            setText(btnTextRes)
            setVisible(true)
            isEnabled = !userWantsToSuggest
            setOnClickListener {
                if (!userWantsToSuggest) {
                    onSuggestionExplanationClickedListener.onWantToSuggestClicked()
                }
            }
        }

        iv_item_generic_explanation_decoration_start.setImageResource(R.drawable.ic_suggestions)
        iv_item_generic_explanation_decoration_end.setImageResource(R.drawable.ic_suggestions)
    }

    companion object {

        fun forParent(
            parent: ViewGroup,
            onSuggestionExplanationClickedListener: OnSuggestionExplanationClickedListener
        ): SuggestionExplanationViewHolder {
            return SuggestionExplanationViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_generic_explanation, parent, false),
                onSuggestionExplanationClickedListener
            )
        }
    }
}

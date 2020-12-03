package at.shockbytes.dante.ui.adapter.suggestions

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.util.adapter.BaseAdapter

class SuggestionsAdapter(
    ctx: Context,
    private val imageLoader: ImageLoader,
    private val onSuggestionActionClickedListener: OnSuggestionActionClickedListener,
    private val onSuggestionExplanationClickedListener: OnSuggestionExplanationClickedListener
) : BaseAdapter<SuggestionsAdapterItem>(ctx) {

    fun updateData(suggestions: List<SuggestionsAdapterItem>) {
        val diffResult = DiffUtil.calculateDiff(SuggestionsDiffUtilCallback(data, suggestions))

        data.clear()
        data.addAll(suggestions)

        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int = data[position].viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<SuggestionsAdapterItem> {

        return when (viewType) {

            R.layout.item_suggestion -> SuggestionViewHolder.forParent(
                parent,
                imageLoader,
                onSuggestionActionClickedListener
            )

            R.layout.item_generic_explanation -> SuggestionExplanationViewHolder.forParent(
                parent,
                onSuggestionExplanationClickedListener
            )

            else -> throw IllegalStateException("Unknown ViewType $viewType in ${this.javaClass.simpleName}")
        }
    }
}
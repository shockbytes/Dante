package at.shockbytes.dante.ui.adapter.suggestions

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.Suggester
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.dante.ui.adapter.OnSuggestionActionClickedListener
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_suggestion.*

class SuggestionsAdapter(
    ctx: Context,
    private val imageLoader: ImageLoader,
    private val onSuggestionActionClickedListener: OnSuggestionActionClickedListener
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

            else -> throw IllegalStateException("Unknown ViewType $viewType in ${this.javaClass.simpleName}")
        }
    }
}
package at.shockbytes.dante.ui.adapter.suggestions

import androidx.recyclerview.widget.DiffUtil

/**
 * Author:  Martin Macheiner
 * Date:    03.12.202
 */
class SuggestionsDiffUtilCallback(
    private val oldList: List<SuggestionsAdapterItem>,
    private val newList: List<SuggestionsAdapterItem>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return when {

            oldItem is SuggestionsAdapterItem.SuggestedBook && newItem is SuggestionsAdapterItem.SuggestedBook -> {
                oldItem.suggestion == newItem.suggestion
            }
            oldItem is SuggestionsAdapterItem.Explanation && newItem is SuggestionsAdapterItem.Explanation -> {
                oldItem == newItem
            }
            else -> {
                // If adapter entities don't match, content can't be the same
                false
            }
        }
    }
}
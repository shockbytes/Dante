package at.shockbytes.dante.ui.adapter.suggestions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemSuggestionBinding
import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.Suggester
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class SuggestionViewHolder(
    override val containerView: View,
    private val imageLoader: ImageLoader,
    private val onSuggestionActionClickedListener: OnSuggestionActionClickedListener
) : BaseAdapter.ViewHolder<SuggestionsAdapterItem>(containerView), LayoutContainer {

    private val vb = ItemSuggestionBinding.bind(containerView)

    private fun context(): Context = containerView.context

    override fun bindToView(content: SuggestionsAdapterItem, position: Int) {
        with((content as SuggestionsAdapterItem.SuggestedBook).suggestion) {
            setupOverflowMenu(suggestionId, suggestion.title)
            setupBook(suggestion)
            setupSuggester(suggester)
            setupRecommendation(recommendation)
            setupBookActionListener(this)
        }
    }

    private fun setupOverflowMenu(suggestionId: String, suggestionTitle: String) {
        vb.ivItemSuggestionReport.setOnClickListener {
            onSuggestionActionClickedListener.onReportBookSuggestion(suggestionId, suggestionTitle)
        }
    }

    private fun setupBook(suggestion: BookSuggestionEntity) {
        vb.tvItemSuggestionAuthor.text = suggestion.author
        vb.tvItemSuggestionTitle.text = suggestion.title
        setThumbnailToView(
            suggestion.thumbnailAddress,
            vb.ivItemSuggestionCover,
            context().resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
        )
    }

    private fun setupSuggester(suggester: Suggester) {
        vb.tvItemSuggestionSuggester.text = context().getString(R.string.suggestion_suggester, suggester.name)
        setThumbnailToView(
            suggester.picture,
            vb.ivItemSuggestionSuggester,
            AppUtils.convertDpInPixel(24, context())
        )
    }

    private fun setupRecommendation(recommendation: String) {
        vb.tvItemSuggestionRecommendation.text = recommendation
    }

    private fun setupBookActionListener(suggestion: Suggestion) {
        vb.btnItemSuggestionAdd.setOnClickListener {
            onSuggestionActionClickedListener.onAddSuggestionToWishlist(suggestion)
        }
    }

    private fun setThumbnailToView(
        url: String?,
        view: ImageView,
        radius: Int
    ) {
        if (!url.isNullOrEmpty()) {
            imageLoader.loadImageWithCornerRadius(context(), url, view, cornerDimension = radius)
        } else {
            // Books with no image will recycle another cover if not cleared here
            view.setImageResource(R.drawable.ic_placeholder)
        }
    }

    companion object {

        fun forParent(
            parent: ViewGroup,
            imageLoader: ImageLoader,
            onSuggestionActionClickedListener: OnSuggestionActionClickedListener
        ): SuggestionViewHolder {
            return SuggestionViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false),
                imageLoader,
                onSuggestionActionClickedListener
            )
        }
    }
}

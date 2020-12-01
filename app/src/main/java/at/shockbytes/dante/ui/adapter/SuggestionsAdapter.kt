package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.suggestions.BookSuggestionEntity
import at.shockbytes.dante.suggestions.Suggester
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_suggestion.*

class SuggestionsAdapter(
    ctx: Context,
    private val imageLoader: ImageLoader,
    private val onSuggestionActionClickedListener: OnSuggestionActionClickedListener
) : BaseAdapter<Suggestion>(ctx) {

    fun updateData(suggestions: List<Suggestion>) {
        data.clear()
        data.addAll(suggestions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Suggestion> {
        return SuggestionViewHolder(inflater.inflate(R.layout.item_suggestion, parent, false))
    }

    private inner class SuggestionViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<Suggestion>(containerView), LayoutContainer {

        override fun bindToView(content: Suggestion, position: Int) {
            with(content) {
                setupOverflowMenu(suggestionId)
                setupBook(suggestion)
                setupSuggester(suggester)
                setupRecommendation(recommendation)
                setupBookActionListener(this)
            }
        }

        private fun setupOverflowMenu(suggestionId: String) {
            iv_item_suggestion_report.setOnClickListener {
                onSuggestionActionClickedListener.onReportBookSuggestion(suggestionId)
            }
        }

        private fun setupBook(suggestion: BookSuggestionEntity) {
            tv_item_suggestion_author.text = suggestion.author
            tv_item_suggestion_title.text = suggestion.title
            setThumbnailToView(
                suggestion.thumbnailAddress,
                iv_item_suggestion_cover,
                context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        }

        private fun setupSuggester(suggester: Suggester) {
            tv_item_suggestion_suggester.text = context.getString(R.string.suggestion_suggester, suggester.name)
            setThumbnailToView(
                suggester.photoUrl,
                iv_item_suggestion_suggester,
                AppUtils.convertDpInPixel(24, context)
            )
        }

        private fun setupRecommendation(recommendation: String) {
            tv_item_suggestion_recommendation.text = recommendation
        }

        private fun setupBookActionListener(suggestion: Suggestion) {
            btn_item_suggestion_add.setOnClickListener {
                onSuggestionActionClickedListener.onAddSuggestionToWishlist(suggestion)
            }
        }

        private fun setThumbnailToView(
            url: String?,
            view: ImageView,
            radius: Int
        ) {
            if (!url.isNullOrEmpty()) {
                imageLoader.loadImageWithCornerRadius(context, url, view, cornerDimension = radius)
            } else {
                // Books with no image will recycle another cover if not cleared here
                view.setImageResource(R.drawable.ic_placeholder)
            }
        }
    }
}
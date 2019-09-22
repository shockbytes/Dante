package at.shockbytes.dante.camera

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.suggestion_picker_item.*

class BookSuggestionPickerAdapter(
    context: Context,
    suggestions: List<BookEntity>,
    private val imageLoader: ImageLoader
) : BaseAdapter<BookEntity>(context) {

    init {
        data = suggestions.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return SuggestionViewHolder(inflater.inflate(R.layout.suggestion_picker_item, parent, false))
    }

    inner class SuggestionViewHolder(
        override val containerView: View
    ) : BaseAdapter<BookEntity>.ViewHolder(containerView), LayoutContainer {

        override fun bindToView(t: BookEntity) {
            with(t) {
                tv_suggestion_picker_item_title.text = title
                tv_suggestion_picker_item_author.text = author

                thumbnailAddress?.let { imageUrl ->
                    imageLoader.loadImageWithCornerRadius(
                        context,
                        imageUrl,
                        iv_suggestion_picker_item_cover,
                        cornerDimension = AppUtils.convertDpInPixel(6, context)
                    )
                }
            }
        }
    }
}

package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookIds
import at.shockbytes.dante.core.book.BookSearchItem
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_book_search_suggestion.*

/**
 * Author: Martin Macheiner
 * Date: 03.02.2018.
 */
class BookSearchSuggestionAdapter(
    context: Context,
    private val imageLoader: ImageLoader,
    private val addClickedListener: (BookSearchItem) -> Unit,
    onItemClickListener: OnItemClickListener<BookSearchItem>
) : BaseAdapter<BookSearchItem>(context, onItemClickListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_book_search_suggestion, parent, false))
    }

    inner class ViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<BookSearchItem>(containerView), LayoutContainer {

        override fun bindToView(content: BookSearchItem, position: Int) {
            item_book_search_suggestion_txt_title.text = content.title
            item_book_search_suggestion_txt_author.text = content.author

            item_book_search_suggestion_btn_add.apply {
                setVisible(BookIds.isValid(content.bookId))
                setOnClickListener {
                    addClickedListener.invoke(content)
                }
            }

            loadImage(content.thumbnailAddress)
        }

        private fun loadImage(thumbnailAddress: String?) {
            if (!thumbnailAddress.isNullOrEmpty()) {
                imageLoader.loadImageWithCornerRadius(
                    context,
                    thumbnailAddress,
                    item_book_search_suggestion_imgview_cover,
                    R.drawable.ic_placeholder,
                    cornerDimension = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                )
            }
        }
    }
}
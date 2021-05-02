package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookIds
import at.shockbytes.dante.core.book.BookSearchItem
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemBookSearchSuggestionBinding
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

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
        return ViewHolder(ItemBookSearchSuggestionBinding.inflate(inflater, parent, false))
    }

    inner class ViewHolder(
        private val vb: ItemBookSearchSuggestionBinding
    ) : BaseAdapter.ViewHolder<BookSearchItem>(vb.root) {

        override fun bindToView(content: BookSearchItem, position: Int) {
            vb.itemBookSearchSuggestionTxtTitle.text = content.title
            vb.itemBookSearchSuggestionTxtAuthor.text = content.author

            vb.itemBookSearchSuggestionBtnAdd.apply {
                setVisible(BookIds.isInvalid(content.bookId))
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
                    vb.itemBookSearchSuggestionImgviewCover,
                    R.drawable.ic_placeholder,
                    cornerDimension = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                )
            }
        }
    }
}
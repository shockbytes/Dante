package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookSearchItem
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotterknife.bindView

/**
 * Author: Martin Macheiner
 * Date: 03.02.2018.
 */
class BookSearchSuggestionAdapter(
    context: Context,
    private val imageLoader: ImageLoader,
    private val addClickedListener: (BookSearchItem) -> Unit
) : BaseAdapter<BookSearchItem>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_book_search_suggestion, parent, false))
    }

    inner class ViewHolder(override val containerView: View) :
        BaseAdapter<BookSearchItem>.ViewHolder(containerView),
        LayoutContainer {

        private val txtTitle: TextView by bindView(R.id.item_book_search_suggestion_txt_title)
        private val txtAuthor: TextView by bindView(R.id.item_book_search_suggestion_txt_author)
        private val btnAdd: Button by bindView(R.id.item_book_search_suggestion_btn_add)
        private val imgViewCover: ImageView by bindView(R.id.item_book_search_suggestion_imgview_cover)

        override fun bindToView(t: BookSearchItem) {
            txtTitle.text = t.title
            txtAuthor.text = t.author

            btnAdd.visibility = if (t.bookId < 0) View.VISIBLE else View.GONE
            btnAdd.setOnClickListener {
                addClickedListener.invoke(t)
            }

            t.thumbnailAddress?.let { address ->
                if (address.isNotEmpty()) {
                    imageLoader.loadImage(context, address, imgViewCover, R.drawable.ic_placeholder)
                }
            }
        }
    }
}
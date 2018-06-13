package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookSearchSuggestion
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.util.adapter.BaseAdapter
import com.squareup.picasso.Picasso
import kotterknife.bindView

/**
 * @author Martin Macheiner
 * Date: 03.02.2018.
 */

class BookSearchSuggestionAdapter(context: Context, extData: MutableList<BookSearchSuggestion>,
                                  private val addClickedListener: (BookSearchSuggestion) -> Unit)
    : BaseAdapter<BookSearchSuggestion>(context, extData) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_book_search_suggestion, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseAdapter<BookSearchSuggestion>.ViewHolder(itemView) {

        private val txtTitle: TextView by bindView(R.id.item_book_search_suggestion_txt_title)
        private val txtAuthor: TextView by bindView(R.id.item_book_search_suggestion_txt_author)
        private val btnAdd: Button by bindView(R.id.item_book_search_suggestion_btn_add)
        private val imgViewCover: ImageView by bindView(R.id.item_book_search_suggestion_imgview_cover)

        override fun bind(t: BookSearchSuggestion) {
            content = t

            txtTitle.text = t.title
            txtAuthor.text = t.author

            btnAdd.visibility = if (t.bookId < 0) View.VISIBLE else View.GONE
            btnAdd.setOnClickListener{
                addClickedListener.invoke(t)
            }

            if (!t.thumbnailAddress.isNullOrEmpty()) {
                Picasso.with(context).load(t.thumbnailAddress)
                        .placeholder(DanteUtils.vector2Drawable(context, R.drawable.ic_placeholder))
                        .into(imgViewCover)
            }
        }

    }

}
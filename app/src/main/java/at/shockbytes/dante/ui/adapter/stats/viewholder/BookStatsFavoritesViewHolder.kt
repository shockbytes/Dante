package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BareBoneBook
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.stats.BookStatsItem
import at.shockbytes.dante.stats.FavoriteAuthor
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_stats_favorites.*
import kotlinx.android.synthetic.main.item_time_line_book.*

class BookStatsFavoritesViewHolder(
    override val containerView: View,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<BookStatsItem>(containerView), LayoutContainer {

    override fun bindToView(content: BookStatsItem, position: Int) {
        with(content as BookStatsItem.Favorites) {
            when (this) {
                BookStatsItem.Favorites.Empty -> {
                    showEmptyState()
                }
                is BookStatsItem.Favorites.Present -> {
                    showReadingDuration(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        TODO("Not implemented yet")
    }

    private fun showReadingDuration(content: BookStatsItem.Favorites.Present) {
        with(content) {
            setFavoriteAuthor(favoriteAuthor)
            setFirstFiveStarBook(firstFiveStarBook)
        }
    }

    private fun setFavoriteAuthor(favoriteAuthor: FavoriteAuthor) {
        // TODO
    }

    private fun setFirstFiveStarBook(firstFiveStarBook: BareBoneBook?) {

        bare_bone_book_view_first_five_star.setVisible(firstFiveStarBook != null)
        tv_item_stats_favorites_first_five_star_header.setVisible(firstFiveStarBook != null)

        firstFiveStarBook?.let {
            bare_bone_book_view_first_five_star.apply {
                setTitle(firstFiveStarBook.title)

                val url = firstFiveStarBook.thumbnailAddress
                if (url != null) {
                    imageLoader.loadImageWithCornerRadius(
                        containerView.context,
                        url,
                        imageView,
                        cornerDimension = containerView.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }
            }
        }
    }
}
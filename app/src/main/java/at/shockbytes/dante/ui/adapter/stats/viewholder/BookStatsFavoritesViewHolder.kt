package at.shockbytes.dante.ui.adapter.stats.viewholder

import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BareBoneBook
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemStatsFavoritesBinding
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.stats.FavoriteAuthor
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BookStatsFavoritesViewHolder(
    private val vb: ItemStatsFavoritesBinding,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<BookStatsViewItem>(vb.root) {

    override fun bindToView(content: BookStatsViewItem, position: Int) {
        with(content as BookStatsViewItem.Favorites) {
            when (this) {
                BookStatsViewItem.Favorites.Empty -> {
                    showEmptyState()
                }
                is BookStatsViewItem.Favorites.Present -> {
                    showReadingDuration(this)
                }
            }
        }
    }

    private fun showEmptyState() {
        vb.itemStatsFavoritesEmpty.root.setVisible(true)
        vb.itemStatsFavoritesContent.setVisible(false)
    }

    private fun showReadingDuration(content: BookStatsViewItem.Favorites.Present) {
        vb.itemStatsFavoritesEmpty.root.setVisible(false)
        vb.itemStatsFavoritesContent.setVisible(true)

        with(content) {
            setFavoriteAuthor(favoriteAuthor)
            setFirstFiveStarBook(firstFiveStarBook)
        }
    }

    private fun setFavoriteAuthor(favoriteAuthor: FavoriteAuthor) {
        vb.multiBareBoneBookFavoriteAuthor.apply {
            setTitle(favoriteAuthor.author)
            setMultipleBookImages(favoriteAuthor.bookUrls, imageLoader)
        }
    }

    private fun setFirstFiveStarBook(firstFiveStarBook: BareBoneBook?) {

        vb.bareBoneBookViewFirstFiveStar.setVisible(firstFiveStarBook != null)
        vb.tvItemStatsFavoritesFirstFiveStarHeader.setVisible(firstFiveStarBook != null)

        firstFiveStarBook?.let {
            vb.bareBoneBookViewFirstFiveStar.apply {
                setTitle(firstFiveStarBook.title)

                val url = firstFiveStarBook.thumbnailAddress
                if (url != null) {
                    imageLoader.loadImageWithCornerRadius(
                        vb.root.context,
                        url,
                        imageView,
                        cornerDimension = vb.root.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                    )
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }
            }
        }
    }
}
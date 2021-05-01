package at.shockbytes.dante.ui.adapter.stats.viewholder

import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BareBoneBook
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemStatsFavoritesBinding
import at.shockbytes.dante.stats.BookStatsViewItem
import at.shockbytes.dante.stats.FavoriteAuthor
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class BookStatsFavoritesViewHolder(
    override val containerView: View,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<BookStatsViewItem>(containerView), LayoutContainer {

    private val vb = ItemStatsFavoritesBinding.bind(containerView)

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
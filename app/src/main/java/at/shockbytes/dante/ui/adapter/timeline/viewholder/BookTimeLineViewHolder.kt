package at.shockbytes.dante.ui.adapter.timeline.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_time_line_book.*

class BookTimeLineViewHolder(
    override val containerView: View,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<TimeLineItem>(containerView), LayoutContainer {

    override fun bindToView(content: TimeLineItem, position: Int) {

        with(content as TimeLineItem.BookTimeLineItem) {
            if ((position % 2) == 0) {
                group_item_time_line_left.setVisible(false)
                group_item_time_line_right.setVisible(true)

                setTitle(tv_item_time_line_book_right, title)
                loadThumbnail(iv_item_time_line_book_right, image)
            } else {
                group_item_time_line_left.setVisible(true)
                group_item_time_line_right.setVisible(false)

                setTitle(tv_item_time_line_book_left, title)
                loadThumbnail(iv_item_time_line_book_left, image)
            }
        }
    }

    private fun setTitle(textView: TextView, title: String) {
        textView.text = title
    }

    private fun loadThumbnail(imageView: ImageView, thumbnailAddress: String?) {
        thumbnailAddress?.let {
            imageLoader.loadImageWithCornerRadius(
                containerView.context,
                thumbnailAddress,
                imageView,
                cornerDimension = containerView.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        }
    }
}
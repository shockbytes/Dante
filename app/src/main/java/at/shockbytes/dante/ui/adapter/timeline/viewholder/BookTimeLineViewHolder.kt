package at.shockbytes.dante.ui.adapter.timeline.viewholder

import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemTimeLineBookBinding
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter

class BookTimeLineViewHolder(
    private val vb: ItemTimeLineBookBinding,
    private val imageLoader: ImageLoader
) : BaseAdapter.ViewHolder<TimeLineItem>(vb.root) {

    override fun bindToView(content: TimeLineItem, position: Int) {

        with(content as TimeLineItem.BookTimeLineItem) {
            if ((position % 2) == 0) {
                vb.groupItemTimeLineLeft.setVisible(false)
                vb.groupItemTimeLineRight.setVisible(true)


                setTitle(vb.tvItemTimeLineBookRight, title)
                loadThumbnail(vb.ivItemTimeLineBookRight, image)
            } else {
                vb.groupItemTimeLineLeft.setVisible(true)
                vb.groupItemTimeLineRight.setVisible(false)

                setTitle(vb.tvItemTimeLineBookLeft, title)
                loadThumbnail(vb.ivItemTimeLineBookLeft, image)
            }
        }
    }

    private fun setTitle(textView: TextView, title: String) {
        textView.text = title
    }

    private fun loadThumbnail(imageView: ImageView, thumbnailAddress: String?) {

        if (thumbnailAddress != null) {
            imageLoader.loadImageWithCornerRadius(
                vb.root.context,
                thumbnailAddress,
                imageView,
                cornerDimension = vb.root.context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder)
        }
    }
}
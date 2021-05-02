package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ItemSimpleBookBinding
import at.shockbytes.util.adapter.BaseAdapter

class SimpleBookAdapter(
    context: Context,
    private val imageLoader: ImageLoader,
    onItemClickListener: OnItemClickListener<BookEntity>
) : BaseAdapter<BookEntity>(context, onItemClickListener) {

    fun updateData(books: List<BookEntity>) {
        data.clear()
        data.addAll(books)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BookEntity> {
        return SimpleBookViewHolder(ItemSimpleBookBinding.inflate(inflater, parent, false))
    }

    inner class SimpleBookViewHolder(
        private val vb: ItemSimpleBookBinding
    ) : BaseAdapter.ViewHolder<BookEntity>(vb.root) {

        override fun bindToView(content: BookEntity, position: Int) {
            with(content) {
                vb.tvItemSimpleBookTitle.text = title
                updateImageThumbnail(thumbnailAddress)
            }
        }

        private fun updateImageThumbnail(address: String?) {

            if (!address.isNullOrEmpty()) {
                imageLoader.loadImageWithCornerRadius(
                    context,
                    address,
                    vb.ivItemSimpleBookCover,
                    cornerDimension = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                )
            } else {
                // Books with no image will recycle another cover if not cleared here
                vb.ivItemSimpleBookCover.setImageResource(R.drawable.ic_placeholder)
            }
        }
    }
}
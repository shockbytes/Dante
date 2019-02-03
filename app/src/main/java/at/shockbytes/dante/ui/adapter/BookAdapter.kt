package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.os.Build
import android.support.constraint.Group
import android.support.v7.util.DiffUtil
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.PopupMenu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.view.BookDiffUtilCallback
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import kotterknife.bindView
import java.util.Collections

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 */
class BookAdapter(
    context: Context,
    private val state: BookState,
    private val imageLoader: ImageLoader,
    private val popupListener: OnBookPopupItemSelectedListener? = null,
    private val showOverflow: Boolean = true
) : BaseAdapter<BookEntity>(context), ItemTouchHelperAdapter {

    interface OnBookPopupItemSelectedListener {

        fun onDelete(b: BookEntity)

        fun onShare(b: BookEntity)

        fun onMoveToUpcoming(b: BookEntity)

        fun onMoveToCurrent(b: BookEntity)

        fun onMoveToDone(b: BookEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter<BookEntity>.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_book, parent, false))
    }

    override fun onItemDismiss(position: Int) {
        val removed = data.removeAt(position)
        onItemMoveListener?.onItemDismissed(removed, position)
    }

    override fun onItemMove(from: Int, to: Int): Boolean {

        // Switch the item within the collection
        if (from < to) {
            for (i in from until to) {
                Collections.swap(data, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(data, i, i - 1)
            }
        }
        notifyItemMoved(from, to)
        onItemMoveListener?.onItemMove(data[from], from, to)

        return true
    }

    override fun onItemMoveFinished() {
        onItemMoveListener?.onItemMoveFinished()
    }

    fun updateData(books: List<BookEntity>) {
        val diffResult = DiffUtil.calculateDiff(BookDiffUtilCallback(data, books))

        data.clear()
        data.addAll(books)

        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : BaseAdapter<BookEntity>.ViewHolder(itemView),
            PopupMenu.OnMenuItemClickListener {

        private val txtTitle by bindView<TextView>(R.id.item_book_txt_title)
        private val txtSubTitle by bindView<TextView>(R.id.item_book_txt_subtitle)
        private val txtAuthor by bindView<TextView>(R.id.item_book_txt_author)
        private val imgViewThumb by bindView<ImageView>(R.id.item_book_img_thumb)
        private val imgBtnOverflow by bindView<ImageButton>(R.id.item_book_img_overflow)

        private val groupProgress by bindView<Group>(R.id.item_book_group_progress)
        private val pbProgress by bindView<ProgressBar>(R.id.item_book_pb)
        private val txtProgress by bindView<TextView>(R.id.item_book_tv_progress)

        init {
            setupOverflowMenu()
        }

        override fun bindToView(t: BookEntity) {
            updateTexts(t)
            updateImageThumbnail(t)
            updateProgress(t)
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {

            // Do not delete book from adapter when user just wants to share it!
            if (item.itemId != R.id.popup_item_share) {
                deleteEntity(content)
            }

            when (item.itemId) {
                R.id.popup_item_move_to_upcoming -> popupListener?.onMoveToUpcoming(content)
                R.id.popup_item_move_to_current -> popupListener?.onMoveToCurrent(content)
                R.id.popup_item_move_to_done -> popupListener?.onMoveToDone(content)
                R.id.popup_item_share -> popupListener?.onShare(content)
                R.id.popup_item_delete -> popupListener?.onDelete(content)
                else -> Unit
            }

            return true
        }

        private fun updateProgress(t: BookEntity) {

            val showProgress = t.reading && t.hasPages

            if (showProgress) {
                val progress = DanteUtils.computePercentage(
                        t.currentPage.toDouble(),
                        t.pageCount.toDouble()
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pbProgress.setProgress(progress, true)
                } else {
                    pbProgress.progress = progress
                }
                txtProgress.text = context.getString(R.string.percentage_formatter, progress)
            }

            groupProgress.setVisible(showProgress)
        }

        private fun updateImageThumbnail(t: BookEntity) {

            if (!t.thumbnailAddress.isNullOrEmpty()) {
                val corners = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                imageLoader.loadImageWithCornerRadius(context, t.thumbnailAddress!!, imgViewThumb,
                        cornerDimension = corners)
            } else {
                // Books with no image will recycle another cover if not cleared here
                imgViewThumb.setImageResource(R.drawable.ic_placeholder)
            }
        }

        private fun updateTexts(t: BookEntity) {
            txtTitle.text = t.title
            txtAuthor.text = t.author
            txtSubTitle.text = t.subTitle
        }

        private fun setupOverflowMenu() {

            val popupMenu = PopupMenu(context, imgBtnOverflow)

            val visibilityOverflow = if (showOverflow) View.VISIBLE else View.GONE
            imgBtnOverflow.visibility = visibilityOverflow

            popupMenu.menuInflater.inflate(R.menu.popup_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this)

            val menuHelper = MenuPopupHelper(context, popupMenu.menu as MenuBuilder, imgBtnOverflow)
            menuHelper.setForceShowIcon(true)

            popupMenu.hideSelectedPopupItem()

            imgBtnOverflow.setOnClickListener { menuHelper.show() }
        }

        private fun PopupMenu.hideSelectedPopupItem() {

            val item = when (state) {

                BookState.READ_LATER -> this.menu.findItem(R.id.popup_item_move_to_upcoming)
                BookState.READING -> this.menu.findItem(R.id.popup_item_move_to_current)
                BookState.READ -> this.menu.findItem(R.id.popup_item_move_to_done)
            }
            item?.isVisible = false
        }
    }
}
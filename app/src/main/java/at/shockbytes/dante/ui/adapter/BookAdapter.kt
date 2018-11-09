package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.PopupMenu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.view.BookDiffUtilCallback
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.ItemTouchHelperAdapter
import kotterknife.bindView
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.*
import kotlin.math.roundToInt


/**
 * @author Martin Macheiner
 * Date: 30.12.2017.
 */

class BookAdapter(context: Context, extData: List<BookEntity>,
                  private val state: BookState,
                  private val imageLoader: ImageLoader,
                  private val popupListener: OnBookPopupItemSelectedListener? = null,
                  private val showOverflow: Boolean = true,
                  private val settings: DanteSettings? = null)
    : BaseAdapter<BookEntity>(context, extData.toMutableList()), ItemTouchHelperAdapter {

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

        private val txtTitle: TextView by bindView(R.id.item_book_txt_title)
        private val txtSubTitle: TextView by bindView(R.id.item_book_txt_subtitle)
        private val txtAuthor: TextView by bindView(R.id.item_book_txt_author)
        private val imgViewThumb: ImageView by bindView(R.id.item_book_img_thumb)
        private val imgBtnOverflow: ImageButton by bindView(R.id.item_book_img_overflow)

        private val containerPageOverlay: View by bindView(R.id.item_book_container_page_overlay)
        private val pbPageOverlay: MaterialProgressBar by bindView(R.id.item_book_pb_page_overlay)
        private val txtPageOverlay: TextView by bindView(R.id.item_book_txt_page_overlay)

        private val popupMenu: PopupMenu

        init {
            // Initialize first to avoid using a lateinit var
            popupMenu = PopupMenu(context, imgBtnOverflow)
            setupOverflowMenu()
        }

        override fun bindToView(t: BookEntity) {
            updateTexts(t)
            updateImageThumbnail(t)
            updatePageOverlay(t)
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {

            content?.let { bookEntity ->
                // Do not delete book from adapter when user just wants to share it!
                if (item.itemId != R.id.popup_item_share) {
                    deleteEntity(bookEntity)
                }

                when (item.itemId) {
                    R.id.popup_item_move_to_upcoming -> popupListener?.onMoveToUpcoming(bookEntity)
                    R.id.popup_item_move_to_current -> popupListener?.onMoveToCurrent(bookEntity)
                    R.id.popup_item_move_to_done -> popupListener?.onMoveToDone(bookEntity)
                    R.id.popup_item_share -> popupListener?.onShare(bookEntity)
                    R.id.popup_item_delete -> popupListener?.onDelete(bookEntity)
                    else -> {
                    }
                }
            }

            return true
        }

        private fun updatePageOverlay(t: BookEntity) {

            if (settings?.pageOverlayEnabled == true && t.reading && t.hasPages) {
                val currentPage = t.currentPage.toDouble()
                val pages = t.pageCount.toDouble()
                val pagePercentage: Int = if (pages > 0) {
                    ((currentPage / pages) * 100).roundToInt()
                } else 0

                pbPageOverlay.progress = pagePercentage
                txtPageOverlay.text = context.getString(R.string.percentage_formatter, pagePercentage)
                containerPageOverlay.visibility = View.VISIBLE
            } else {
                containerPageOverlay.visibility = View.GONE
            }
        }

        private fun updateImageThumbnail(t: BookEntity) {

            if (!t.thumbnailAddress.isNullOrEmpty()) {
                val corners = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                imageLoader.loadImageWithCornerRadius(context, t.thumbnailAddress!!, imgViewThumb,
                        cornerDimension = corners)
            } else
            // Books with no image will recycle another cover if not cleared here
                imgViewThumb.setImageResource(R.drawable.ic_placeholder)
        }

        private fun updateTexts(t: BookEntity) {
            txtTitle.text = t.title
            txtAuthor.text = t.author
            txtSubTitle.text = t.subTitle
        }

        private fun setupOverflowMenu() {

            val visibilityOverflow = if (showOverflow) View.VISIBLE else View.GONE
            imgBtnOverflow.visibility = visibilityOverflow

            popupMenu.menuInflater.inflate(R.menu.popup_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this)
            DanteUtils.tryShowIconsInPopupMenu(popupMenu)
            hideSelectedPopupItem()

            imgBtnOverflow.setOnClickListener { popupMenu.show() }
        }

        private fun hideSelectedPopupItem() {

            val item = when (state) {

                BookState.READ_LATER -> popupMenu.menu.findItem(R.id.popup_item_move_to_upcoming)
                BookState.READING -> popupMenu.menu.findItem(R.id.popup_item_move_to_current)
                BookState.READ -> popupMenu.menu.findItem(R.id.popup_item_move_to_done)
            }
            item?.isVisible = false
        }

    }

}
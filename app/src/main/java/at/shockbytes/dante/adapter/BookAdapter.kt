package at.shockbytes.dante.adapter

import android.content.Context
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.util.books.Book
import at.shockbytes.util.adapter.BaseAdapter
import com.squareup.picasso.Picasso
import kotterknife.bindView

/**
 * @author Martin Macheiner
 * Date: 30.12.2017.
 */

class BookAdapter(context: Context, extData: List<Book>, private val state: Book.State,
                  private val popupListener: OnBookPopupItemSelectedListener?,
                  private val showOverflow: Boolean) : BaseAdapter<Book>(context, extData.toMutableList()) {

    interface OnBookPopupItemSelectedListener {

        fun onDelete(b: Book)

        fun onShare(b: Book)

        fun onMoveToUpcoming(b: Book)

        fun onMoveToCurrent(b: Book)

        fun onMoveToDone(b: Book)
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BaseAdapter<Book>.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.listitem_book, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseAdapter<Book>.ViewHolder(itemView),
            PopupMenu.OnMenuItemClickListener {

        private val txtTitle: TextView by bindView(R.id.listitem_book_txt_title)
        private val txtSubTitle: TextView by bindView(R.id.listitem_book_txt_subtitle)
        private val txtAuthor: TextView by bindView(R.id.listitem_book_txt_author)
        private val imgViewThumb: ImageView by bindView(R.id.listitem_book_img_thumb)
        private val imgBtnOverflow: ImageButton by bindView(R.id.listitem_book_img_overflow)

        private val popupMenu: PopupMenu

        init {

            val visibilityOverflow = if (showOverflow) View.VISIBLE else View.GONE
            imgBtnOverflow.visibility = visibilityOverflow

            popupMenu = PopupMenu(context, imgBtnOverflow)
            popupMenu.menuInflater.inflate(R.menu.popup_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this)
            tryShowIconsInPopupMenu(popupMenu)
            hideSelectedPopupItem()

            imgBtnOverflow.setOnClickListener { popupMenu.show() }
        }

        private fun hideSelectedPopupItem() {

            val item = when (state) {

                Book.State.READ_LATER -> popupMenu.menu.findItem(R.id.popup_item_move_to_upcoming)

                Book.State.READING -> popupMenu.menu.findItem(R.id.popup_item_move_to_current)

                Book.State.READ -> popupMenu.menu.findItem(R.id.popup_item_move_to_done)
            }
            item?.isVisible = false
        }

        override fun bind(t: Book) {
            content = t

            txtTitle.text = t.title
            txtAuthor.text = t.author
            txtSubTitle.text = t.subTitle

            val thumbnailAddress = t.thumbnailAddress
            if (thumbnailAddress != null && !thumbnailAddress.isEmpty()) {
                Picasso.with(context).load(thumbnailAddress)
                        .placeholder(R.drawable.ic_placeholder).into(imgViewThumb)
            } else {
                // Books with no image will recycle another cover if not cleared here
                imgViewThumb.setImageResource(R.drawable.ic_placeholder)
            }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {

            return if (content != null) {
                // Do not delete book from adapter when user just wants to share it!
                if (item.itemId != R.id.popup_item_share) {
                    deleteEntity(content!!)
                }

                when (item.itemId) {

                    R.id.popup_item_move_to_upcoming -> popupListener?.onMoveToUpcoming(content!!)

                    R.id.popup_item_move_to_current -> popupListener?.onMoveToCurrent(content!!)

                    R.id.popup_item_move_to_done -> popupListener?.onMoveToDone(content!!)

                    R.id.popup_item_share -> popupListener?.onShare(content!!)

                    R.id.popup_item_delete -> popupListener?.onDelete(content!!)
                }
                 true
            } else {
                false
            }
        }
    }

    private fun tryShowIconsInPopupMenu(menu: PopupMenu) {

        try {
            val fieldPopup = menu.javaClass.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val popup = fieldPopup.get(menu) as MenuPopupHelper
            popup.setForceShowIcon(true)
        } catch (e: Exception) {
            Log.d("Dante", "Cannot force to show icons in popupmenu")
        }

    }

}
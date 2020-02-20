package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_book_label_management.*

class LabelManagementAdapter(
    context: Context,
    onItemClickListener: OnItemClickListener<BookLabel>,
    private val onLabelActionClickedListener: OnLabelActionClickedListener
) : BaseAdapter<BookLabel>(context, onItemClickListener) {

    fun updateData(labels: List<BookLabel>) {
        data.clear()
        data.addAll(labels)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BookLabel> {
        val view = inflater.inflate(R.layout.item_book_label_management, parent, false)
        return LabelManagementViewHolder(view)
    }

    inner class LabelManagementViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<BookLabel>(containerView), LayoutContainer {

        override fun bindToView(content: BookLabel, position: Int) {
            with(content) {
                tv_item_label_management.text = title

                DanteUtils.createRoundedBitmapFromColor(
                    context,
                    AppUtils.convertDpInPixel(32, context),
                    Color.parseColor(hexColor)
                ).let { rbd ->
                    iv_item_label_management.setImageDrawable(rbd)
                }

                setupOverflowMenu(this)
            }
        }

        private fun setupOverflowMenu(label: BookLabel) {

            val popupMenu = PopupMenu(context, btn_item_label_management_overflow)

            popupMenu.menuInflater.inflate(R.menu.menu_label_overflow, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_label_item_delete -> {
                        onLabelActionClickedListener.onLabelDeleted(label)
                    }
                }
                true
            }

            val menuHelper = MenuPopupHelper(context, popupMenu.menu as MenuBuilder, btn_item_label_management_overflow)
            menuHelper.setForceShowIcon(true)

            btn_item_label_management_overflow.setOnClickListener {
                menuHelper.show()
            }
        }
    }
}
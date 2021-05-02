package at.shockbytes.dante.ui.adapter

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.databinding.ItemBookLabelManagementBinding
import at.shockbytes.dante.util.isNightModeEnabled
import at.shockbytes.dante.util.layoutInflater
import at.shockbytes.util.adapter.BaseAdapter
import kotlin.math.roundToInt

class LabelManagementAdapter(
    context: Context,
    onItemClickListener: OnItemClickListener<BookLabel>,
    private val isLabelColorEditEnabled: Boolean,
    private val onLabelActionClickedListener: OnLabelActionClickedListener
) : BaseAdapter<BookLabel>(context, onItemClickListener) {

    private val isNightModeEnabled = context.isNightModeEnabled()

    fun updateData(labels: List<BookLabel>) {
        data.clear()
        data.addAll(labels)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<BookLabel> {
        val layoutWidth = (parent.width * SCALE_FACTOR_WIDTH).roundToInt()
        return LabelManagementViewHolder(LabelManagementItemView(parent.context, layoutWidth))
    }

    private class LabelManagementItemView(
        context: Context,
        layoutWidth: Int
    ) : ConstraintLayout(context) {

        private val vb = ItemBookLabelManagementBinding.inflate(context.layoutInflater(), this, true)

        val titleView: TextView
            get() = vb.tvItemLabelManagement

        val imageView: AppCompatImageView
            get() = vb.ivItemLabelManagement

        val overflowButton: ImageView
            get() = vb.ivItemLabelManagementOverflow

        init {
            layoutParams = LayoutParams(
                layoutWidth,
                RecyclerView.LayoutParams.MATCH_PARENT
            )

            layoutTransition = LayoutTransition() // android:animateLayoutChanges="true"
            isActivated = false
        }

        override fun setActivated(activated: Boolean) {
            val isChanging = activated != isActivated
            super.setActivated(activated)

            if (isChanging) {
                vb.bgSelectionLabelManagement.onChanged(activated)
            }
        }
    }

    private inner class LabelManagementViewHolder(
        val view: LabelManagementItemView,
    ) : BaseAdapter.ViewHolder<BookLabel>(view) {

        override fun bindToView(content: BookLabel, position: Int) {
            with(content) {
                view.titleView.text = title

                val color = if (isNightModeEnabled) {
                    labelHexColor.asDesaturatedColorInt(desaturateBy = 0.25f)
                } else {
                    labelHexColor.asColorInt()
                }

                view.imageView.imageTintList = ColorStateList.valueOf(color)

                setupOverflowMenu(this)
            }
        }

        private fun setupOverflowMenu(label: BookLabel) {

            val popupMenu = PopupMenu(context, view.overflowButton)

            popupMenu.menuInflater.inflate(R.menu.menu_label_overflow, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_label_item_delete -> {
                        onLabelActionClickedListener.onLabelDeleted(label)
                    }
                    R.id.popup_label_item_edit_color -> {
                        onLabelActionClickedListener.onLabelColorEdit(label)
                    }
                }
                true
            }

            // Temporarily disable visibility of edit color button
            popupMenu.menu.findItem(R.id.popup_label_item_edit_color)?.isVisible = isLabelColorEditEnabled

            val menuHelper = MenuPopupHelper(context, popupMenu.menu as MenuBuilder, view.overflowButton)
            menuHelper.setForceShowIcon(true)

            view.overflowButton.setOnClickListener {
                menuHelper.show()
            }
        }
    }

    companion object {
        private const val SCALE_FACTOR_WIDTH = 0.65f
    }
}
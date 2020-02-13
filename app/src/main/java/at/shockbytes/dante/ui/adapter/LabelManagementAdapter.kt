package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.barcode.util.BitmapUtils
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_book_label_management.*

class LabelManagementAdapter(
    context: Context,
    onItemClickListener: OnItemClickListener<BookLabel>,
    private val onLabelDeleteClickListener: ((BookLabel) -> Unit)
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

                BitmapUtils.createRoundedBitmapFromColor(
                    context,
                    AppUtils.convertDpInPixel(32, context),
                    Color.parseColor(hexColor)
                ).let { rbd ->
                    iv_item_label_management.setImageDrawable(rbd)
                }

                btn_item_label_management_delete.setOnClickListener {
                    onLabelDeleteClickListener(this)
                }
            }
        }
    }
}
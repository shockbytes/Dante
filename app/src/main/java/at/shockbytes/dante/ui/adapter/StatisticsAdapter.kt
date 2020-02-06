package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.statistics.StatisticsDisplayItem
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_statistics_data.*
import kotlinx.android.synthetic.main.item_statistics_header.*
import java.lang.IllegalArgumentException

@Deprecated("Use StatsAdapter instead")
class StatisticsAdapter(context: Context) : BaseAdapter<StatisticsDisplayItem>(context) {

    fun updateData(items: List<StatisticsDisplayItem>) {
        data.clear()
        data.addAll(items)

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is StatisticsDisplayItem.StatisticsHeaderItem) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<StatisticsDisplayItem> {
        return when (viewType) {
            VIEW_TYPE_DATA -> {
                DataViewHolder(inflater.inflate(R.layout.item_statistics_data, parent, false))
            }
            VIEW_TYPE_HEADER -> {
                HeaderViewHolder(inflater.inflate(R.layout.item_statistics_header, parent, false))
            }
            else -> throw IllegalArgumentException("View type must be either 0 or 1")
        }
    }

    inner class DataViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<StatisticsDisplayItem>(containerView), LayoutContainer {

        override fun bindToView(content: StatisticsDisplayItem, position: Int) {
            with(content as StatisticsDisplayItem.StatisticsDataItem) {
                if (align == StatisticsDisplayItem.Align.START) {
                    item_statistics_data_icon_start.setImageResource(icon)
                    item_statistics_data_icon_start.setColorFilter(ContextCompat.getColor(context, tintColorRes))
                    item_statistics_data_icon_end.visibility = View.GONE
                    item_statistics_data_txt_title.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                } else {
                    item_statistics_data_icon_end.setImageResource(icon)
                    item_statistics_data_icon_end.setColorFilter(ContextCompat.getColor(context, tintColorRes))
                    item_statistics_data_icon_start.visibility = View.GONE
                    item_statistics_data_txt_title.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }

                when (messageArgs.size) {
                    1 -> item_statistics_data_txt_title.text = context.getString(title, messageArgs[0])
                    2 -> item_statistics_data_txt_title.text = context.getString(title, messageArgs[0], messageArgs[1])
                }
            }
        }
    }

    inner class HeaderViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<StatisticsDisplayItem>(containerView), LayoutContainer {

        override fun bindToView(content: StatisticsDisplayItem, position: Int) {
            with(content as StatisticsDisplayItem.StatisticsHeaderItem) {
                item_statistics_header_icon.setImageResource(icon)
                item_statistics_header_title.text = context.getString(title)
            }
        }
    }

    companion object {

        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_DATA = 1
    }
}
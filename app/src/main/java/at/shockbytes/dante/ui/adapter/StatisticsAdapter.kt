package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.book.statistics.StatisticsDisplayItem
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotterknife.bindView
import java.lang.IllegalArgumentException

class StatisticsAdapter(context: Context) : BaseAdapter<StatisticsDisplayItem>(context) {

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is StatisticsDisplayItem.StatisticsHeaderItem) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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

    inner class DataViewHolder(override val containerView: View) :
        BaseAdapter<StatisticsDisplayItem>.ViewHolder(containerView),
        LayoutContainer {

        private val imgViewIconStart by bindView<ImageView>(R.id.item_statistics_data_icon_start)
        private val imgViewIconEnd by bindView<ImageView>(R.id.item_statistics_data_icon_end)
        private val txtTitle by bindView<TextView>(R.id.item_statistics_data_txt_title)

        override fun bindToView(t: StatisticsDisplayItem) {
            t as StatisticsDisplayItem.StatisticsDataItem

            if (t.align == StatisticsDisplayItem.Align.START) {
                imgViewIconStart.setImageResource(t.icon)
                imgViewIconEnd.visibility = View.GONE
                txtTitle.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            } else {
                imgViewIconEnd.setImageResource(t.icon)
                imgViewIconStart.visibility = View.GONE
                txtTitle.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }

            when (t.messageArgs.size) {
                1 -> txtTitle.text = context.getString(t.title, t.messageArgs[0])
                2 -> txtTitle.text = context.getString(t.title, t.messageArgs[0], t.messageArgs[1])
            }
        }
    }

    inner class HeaderViewHolder(override val containerView: View) :
        BaseAdapter<StatisticsDisplayItem>.ViewHolder(containerView),
        LayoutContainer {

        private val imgViewIcon by bindView<ImageView>(R.id.item_statistics_header_icon)
        private val txtTitle by bindView<TextView>(R.id.item_statistics_header_title)

        override fun bindToView(t: StatisticsDisplayItem) {
            t as StatisticsDisplayItem.StatisticsHeaderItem
            imgViewIcon.setImageResource(t.icon)
            txtTitle.text = context.getString(t.title)
        }
    }

    companion object {

        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_DATA = 1
    }
}
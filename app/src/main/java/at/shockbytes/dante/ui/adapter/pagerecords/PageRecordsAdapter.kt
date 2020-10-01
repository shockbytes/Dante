package at.shockbytes.dante.ui.adapter.pagerecords

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

class PageRecordsAdapter(context: Context): BaseAdapter<PageRecordDetailItem>(context) {

    fun updateData(updatedData: List<PageRecordDetailItem>) {
        data.clear()
        data.addAll(updatedData)
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): ViewHolder<PageRecordDetailItem> {
        return PageRecordsViewHolder(inflater.inflate(R.layout.item_page_records_detail, parent, false))
    }

    inner class PageRecordsViewHolder(
            override val containerView: View
    ): BaseAdapter.ViewHolder<PageRecordDetailItem>(containerView), LayoutContainer {

        override fun bindToView(content: PageRecordDetailItem, position: Int) {
            with(content) {
                // TODO
            }
        }
    }
}
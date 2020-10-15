package at.shockbytes.dante.ui.adapter.pagerecords

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_page_records_detail.*

class PageRecordsAdapter(
    context: Context,
    private val onItemDeletedListener: (PageRecord) -> Unit
) : BaseAdapter<PageRecordDetailItem>(context) {

    fun updateData(updatedData: List<PageRecordDetailItem>) {

        data.clear()
        data.addAll(updatedData)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<PageRecordDetailItem> {
        val view = inflater.inflate(R.layout.item_page_records_detail, parent, false)
        return PageRecordsViewHolder(view)
    }

    inner class PageRecordsViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<PageRecordDetailItem>(containerView), LayoutContainer {

        override fun bindToView(content: PageRecordDetailItem, position: Int) {
            with(content) {
                tv_item_page_records_detail_date.text = formattedDate
                tv_item_page_records_detail_pages.text = formattedPagesRead

                btn_item_page_records_detail_delete.setOnClickListener {
                    onItemDeletedListener(pageRecord)
                }
            }
        }
    }
}
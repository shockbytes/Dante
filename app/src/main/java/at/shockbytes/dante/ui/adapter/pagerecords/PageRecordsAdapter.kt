package at.shockbytes.dante.ui.adapter.pagerecords

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.PageRecord
import at.shockbytes.dante.databinding.ItemPageRecordsDetailBinding
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer

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

        private val vb = ItemPageRecordsDetailBinding.bind(containerView)

        override fun bindToView(content: PageRecordDetailItem, position: Int) {
            with(content) {
                vb.tvItemPageRecordsDetailDate.text = formattedDate
                vb.tvItemPageRecordsDetailPages.text = formattedPagesRead

                vb.btnItemPageRecordsDetailDelete.setOnClickListener {
                    onItemDeletedListener(pageRecord)
                }
            }
        }
    }
}
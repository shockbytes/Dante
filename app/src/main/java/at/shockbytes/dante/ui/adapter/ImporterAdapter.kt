package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import at.shockbytes.dante.R
import at.shockbytes.dante.importer.Importer
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_importer.*

class ImporterAdapter(
    context: Context,
    items: Array<Importer>,
    private val onImportClickedListener: ((Importer) -> Unit)
) : BaseAdapter<Importer>(context) {

    init {
        data.addAll(items.toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Importer> {
        return ImporterViewHolder(inflater.inflate(R.layout.item_importer, parent, false))
    }

    inner class ImporterViewHolder(
        override val containerView: View
    ) : BaseAdapter.ViewHolder<Importer>(containerView), LayoutContainer {

        override fun bindToView(content: Importer, position: Int) {
            with(content) {
                iv_item_import.setImageResource(icon)
                tv_item_import_title.setText(title)
                tv_item_import_description.setText(description)

                btn_item_import.setOnClickListener {
                    onImportClickedListener(this)
                }
            }
        }
    }
}
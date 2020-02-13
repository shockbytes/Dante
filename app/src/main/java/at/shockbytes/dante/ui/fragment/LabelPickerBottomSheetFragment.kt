package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.LabelManagementAdapter
import at.shockbytes.dante.ui.fragment.dialog.CreateLabelDialogFragment
import at.shockbytes.dante.ui.viewmodel.LabelManagementViewModel
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_label_picker_bottom_sheet.*
import javax.inject.Inject

class LabelPickerBottomSheetFragment : BaseBottomSheetFragment() {

    override val layoutRes: Int = R.layout.fragment_label_picker_bottom_sheet

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LabelManagementViewModel

    private var attachedLabels: AttachedLabels by argument()

    private var onLabelSelectedListener: ((BookLabel) -> Unit)? = null

    private val labelAdapter: LabelManagementAdapter by lazy {
        LabelManagementAdapter(
            requireContext(),
            object : BaseAdapter.OnItemClickListener<BookLabel> {
                override fun onItemClick(content: BookLabel, position: Int, v: View) {
                    onLabelSelectedListener?.invoke(content)
                    dismiss()
                }
            },
            onLabelDeleteClickListener = viewModel::deleteBookLabel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestAvailableLabels(attachedLabels.labels)
        viewModel.getBookLabels().observe(this, Observer(labelAdapter::updateData))
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {
        rv_pick_labels.apply {
            adapter = labelAdapter
        }

        btn_create_new_label.setOnClickListener {
            CreateLabelDialogFragment.newInstance()
                .setOnApplyListener(viewModel::createNewBookLabel)
                .show(childFragmentManager, "create-label-dialog-fragment")
        }
    }

    fun setOnLabelSelectedListener(listener: ((BookLabel) -> Unit)): LabelPickerBottomSheetFragment {
        return this.apply {
            onLabelSelectedListener = listener
        }
    }

    companion object {

        fun newInstance(alreadyAttachedLabels: List<BookLabel>): LabelPickerBottomSheetFragment {
            return LabelPickerBottomSheetFragment().apply {
                attachedLabels = AttachedLabels(alreadyAttachedLabels)
            }
        }

        @Parcelize
        private data class AttachedLabels(val labels: List<BookLabel>) : Parcelable
    }
}
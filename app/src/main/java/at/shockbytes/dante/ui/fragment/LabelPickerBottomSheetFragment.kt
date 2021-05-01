package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.PagerSnapHelper
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.databinding.FragmentLabelPickerBottomSheetBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.LabelManagementAdapter
import at.shockbytes.dante.ui.adapter.OnLabelActionClickedListener
import at.shockbytes.dante.ui.fragment.dialog.CreateLabelDialogFragmentWrapper
import at.shockbytes.dante.ui.viewmodel.LabelManagementViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.view.ProminentLayoutManager
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.adapter.BaseAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

class LabelPickerBottomSheetFragment : BaseBottomSheetFragment<FragmentLabelPickerBottomSheetBinding>() {

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
            isLabelColorEditEnabled = false,
            object : OnLabelActionClickedListener {
                override fun onLabelDeleted(label: BookLabel) {
                    viewModel.deleteBookLabel(label)
                }

                override fun onLabelColorEdit(label: BookLabel) {
                    // TODO Edit color of label
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentLabelPickerBottomSheetBinding {
        return FragmentLabelPickerBottomSheetBinding.inflate(inflater, root, attachToRoot)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestAvailableLabels(attachedLabels.labels)
        viewModel.getBookLabelState().observe(this, Observer(::handleLabelState))

        viewModel.onCreateNewLabelRequest
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ labels ->
                CreateLabelDialogFragmentWrapper.newInstance(labels)
                    .setOnApplyListener(viewModel::createNewBookLabel)
                    .show(this)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    private fun handleLabelState(state: LabelManagementViewModel.LabelState) {
        when (state) {
            LabelManagementViewModel.LabelState.Empty -> {
                vb.tvPickLabelsEmpty.setVisible(true)
                vb.rvPickLabels.setVisible(false)
            }
            is LabelManagementViewModel.LabelState.Present -> {
                vb.tvPickLabelsEmpty.setVisible(false)
                vb.rvPickLabels.setVisible(true)

                labelAdapter.updateData(state.labels)
            }
        }
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {
        vb.rvPickLabels.apply {
            PagerSnapHelper().attachToRecyclerView(this)
            layoutManager = ProminentLayoutManager(requireContext())
            adapter = labelAdapter
        }

        vb.btnCreateNewLabel.setOnClickListener {
            viewModel.requestCreateNewLabel()
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
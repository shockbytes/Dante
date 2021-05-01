package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.FragmentOnlineStorageBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.viewmodel.OnlineStorageViewModel
import at.shockbytes.dante.util.viewModelOf
import javax.inject.Inject

class OnlineStorageFragment : BaseFragment<FragmentOnlineStorageBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentOnlineStorageBinding {
        return FragmentOnlineStorageBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: OnlineStorageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {

        vb.btnOnlineStorageInterested.setOnClickListener {
            viewModel.userIsInterested()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

        viewModel.requestButtonState()
        viewModel.getButtonState().observe(this, Observer(::updateInterestedButton))
    }

    private fun updateInterestedButton(buttonState: OnlineStorageViewModel.InterestedButtonState) {

        when (buttonState) {
            OnlineStorageViewModel.InterestedButtonState.DEFAULT -> {
                vb.btnOnlineStorageInterested.apply {
                    isEnabled = true
                    setText(R.string.online_storage_interested)
                }
            }
            OnlineStorageViewModel.InterestedButtonState.INTERESTED -> {
                vb.btnOnlineStorageInterested.apply {
                    isEnabled = false
                    setText(R.string.online_storage_already_interested)
                }
            }
        }
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): OnlineStorageFragment {
            return OnlineStorageFragment()
        }
    }
}
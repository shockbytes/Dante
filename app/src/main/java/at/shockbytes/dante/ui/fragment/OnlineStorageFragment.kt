package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.viewmodel.OnlineStorageViewModel
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.fragment_online_storage.*
import javax.inject.Inject

class OnlineStorageFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_online_storage

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: OnlineStorageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {

        btn_online_storage_interested.setOnClickListener {
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
                btn_online_storage_interested.apply {
                    isEnabled = true
                    setText(R.string.online_storage_interested)
                }
            }
            OnlineStorageViewModel.InterestedButtonState.INTERESTED -> {
                btn_online_storage_interested.apply {
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
package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.adapter.BackupStorageProviderAdapter
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import at.shockbytes.dante.util.Priority
import at.shockbytes.dante.util.addTo
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.view.EqualSpaceItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_backup_backup.*
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupBackupFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_backup_backup

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)[BackupViewModel::class.java]
    }

    override fun setupViews() = Unit

    override fun bindViewModel() {

        viewModel.getLastBackupTime().observe(this, Observer { lastBackup ->
            tv_fragment_backup_last_backup.text = getString(R.string.last_backup, lastBackup)
        })

        viewModel.getBackupProviders().observe(this, Observer { providers ->
            setupBackupProviderUI(providers)
        })

        viewModel.makeBackupEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                when (state) {
                    is BackupViewModel.State.Success -> {
                        showToast(getString(R.string.backup_created), showLong = false)
                    }
                    is BackupViewModel.State.Error -> {
                        showSnackbar(getString(R.string.backup_not_created))
                    }
                }
            }
            .addTo(compositeDisposable)
    }

    override fun unbindViewModel() = Unit

    private fun setupBackupProviderUI(providers: List<BackupStorageProvider>) {

        rv_fragment_backup_providers.apply {
            layoutManager = GridLayoutManager(requireContext(), 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return (rv_fragment_backup_providers.adapter as BackupStorageProviderAdapter)
                            .data[position].priority.run {
                            when (this) {
                                Priority.LOW -> 1
                                Priority.MEDIUM -> 1
                                Priority.HIGH -> 2
                            }
                        }
                    }
                }
            }
            adapter = BackupStorageProviderAdapter(requireContext()).apply {
                data = providers.sortedByDescending { it.priority }.toMutableList()
                onItemClickListener = object : BaseAdapter.OnItemClickListener<BackupStorageProvider> {
                    override fun onItemClick(t: BackupStorageProvider, v: View) {
                        viewModel.makeBackup(t)
                    }
                }
            }
            addItemDecoration(EqualSpaceItemDecoration(8))
        }
    }

    companion object {

        fun newInstance(): BackupBackupFragment {
            return BackupBackupFragment()
        }
    }
}
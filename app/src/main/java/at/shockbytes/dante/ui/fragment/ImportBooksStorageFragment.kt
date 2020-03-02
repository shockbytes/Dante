package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.importer.Importer
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.ImporterAdapter
import at.shockbytes.dante.ui.viewmodel.ImportBooksStorageViewModel
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.fragment_import_books_storage.*
import javax.inject.Inject

class ImportBooksStorageFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_import_books_storage

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ImportBooksStorageViewModel

    private val importAdapter: ImporterAdapter by lazy {
        ImporterAdapter(requireContext(), Importer.values(), viewModel::startImport)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {
        rv_fragment_import.apply {
            adapter = importAdapter
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

        viewModel.getImportState().observe(this, Observer(::handleImportState))
    }

    private fun handleImportState(importState: ImportBooksStorageViewModel.ImportState) {

        when (importState) {
            ImportBooksStorageViewModel.ImportState.ImportStarted -> {
                showSnackbar("Import started")
            }
            is ImportBooksStorageViewModel.ImportState.Parsed -> TODO()
            is ImportBooksStorageViewModel.ImportState.Error -> TODO()
            ImportBooksStorageViewModel.ImportState.Imported -> TODO()
        }
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): ImportBooksStorageFragment {
            return ImportBooksStorageFragment()
        }
    }
}

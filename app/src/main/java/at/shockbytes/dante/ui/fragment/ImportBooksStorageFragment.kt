package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.viewmodel.ImportBooksStorageViewModel
import at.shockbytes.dante.util.viewModelOf
import javax.inject.Inject

class ImportBooksStorageFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_import_books_storage

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ImportBooksStorageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): ImportBooksStorageFragment {
            return ImportBooksStorageFragment()
        }
    }
}

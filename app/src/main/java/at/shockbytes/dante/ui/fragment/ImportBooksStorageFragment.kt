package at.shockbytes.dante.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.importer.Importer
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.ImporterAdapter
import at.shockbytes.dante.ui.viewmodel.ImportBooksStorageViewModel
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.viewModelOf
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_import_books_storage.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.charset.Charset
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

        viewModel.getImportState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleImportState, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    private fun handleImportState(importState: ImportBooksStorageViewModel.ImportState) {

        when (importState) {
            is ImportBooksStorageViewModel.ImportState.AskForFile -> {

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .setType(importState.mimeType)
                    .addCategory(Intent.CATEGORY_OPENABLE)

                startActivityForResult(intent, REQUEST_SAF)
            }
            is ImportBooksStorageViewModel.ImportState.Parsed -> {
                // TODO Open import approval dialog
                viewModel.import()
            }
            is ImportBooksStorageViewModel.ImportState.Error -> {
                showSnackbar(importState.throwable.toString())
            }
            ImportBooksStorageViewModel.ImportState.Imported -> {
                showToast("IMPORT SUCCESS!!!")
                viewModel.confirmImport()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SAF) {
            if (resultCode == RESULT_OK && data != null) {
                runBlocking(Dispatchers.Unconfined) {
                    data.data?.read(requireContext()).let(viewModel::parseFromString)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    suspend fun Uri.read(context: Context): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(this@read)?.use { stream -> stream.readText() }
            ?: throw IllegalStateException("Could not open $this")
    }

    private fun InputStream.readText(charset: Charset = Charsets.UTF_8): String = readBytes().toString(charset)

    override fun unbindViewModel() = Unit

    companion object {

        private const val REQUEST_SAF = 0x2349

        fun newInstance(): ImportBooksStorageFragment {
            return ImportBooksStorageFragment()
        }
    }
}

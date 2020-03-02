package at.shockbytes.dante.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.importer.Importer
import javax.inject.Inject

class ImportBooksStorageViewModel @Inject constructor() : BaseViewModel() {

    sealed class ImportState {

        object Idle : ImportState()

        object ImportStarted : ImportState()

        data class Parsed(
            val booksImported: Int
        ) : ImportState()

        data class Error(val throwable: Throwable) : ImportState()

        object Imported : ImportState()
    }

    private val importState = MutableLiveData<ImportState>()
    fun getImportState(): LiveData<ImportState> = importState

    init {
        importState.postValue(ImportState.Idle)
    }

    fun startImport(importer: Importer) {
        // TODO
        if (importState.value == ImportState.Idle) {
            importState.postValue(ImportState.ImportStarted)
        }
    }
}
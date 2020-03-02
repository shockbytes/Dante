package at.shockbytes.dante.ui.viewmodel

import at.shockbytes.dante.importer.ImportRepository
import at.shockbytes.dante.importer.Importer
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ImportBooksStorageViewModel @Inject constructor(
    private val importRepository: ImportRepository
) : BaseViewModel() {

    sealed class ImportState {

        object Idle : ImportState()

        data class AskForFile(val mimeType: String) : ImportState()

        data class Parsed(
            val booksImported: Int
        ) : ImportState()

        data class Error(val throwable: Throwable) : ImportState()

        object Imported : ImportState()
    }

    private val importState = PublishSubject.create<ImportState>()
    fun getImportState(): Observable<ImportState> = importState

    init {
        importState.onNext(ImportState.Idle)
    }

    fun startImport(importer: Importer) {
        // TODO
        importState.onNext(ImportState.AskForFile(importer.mimeType))
    }

    fun importFromString(content: String?) {

    }
}
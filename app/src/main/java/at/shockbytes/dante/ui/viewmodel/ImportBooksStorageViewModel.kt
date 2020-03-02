package at.shockbytes.dante.ui.viewmodel

import at.shockbytes.dante.importer.ImportRepository
import at.shockbytes.dante.importer.ImportStats
import at.shockbytes.dante.importer.Importer
import at.shockbytes.dante.util.ExceptionHandlers
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ImportBooksStorageViewModel @Inject constructor(
    private val importRepository: ImportRepository
) : BaseViewModel() {

    sealed class ImportState {

        object Idle : ImportState()

        data class AskForFile(val mimeType: String) : ImportState()

        data class Parsed(
            val importStats: ImportStats
        ) : ImportState()

        data class Error(val throwable: Throwable) : ImportState()

        object Imported : ImportState()
    }

    private val importState = PublishSubject.create<ImportState>()
    fun getImportState(): Observable<ImportState> = importState

    private var selectedImporter: Importer? = null

    init {
        importState.onNext(ImportState.Idle)
    }

    fun startImport(importer: Importer) {
        selectedImporter = importer
        importState.onNext(ImportState.AskForFile(importer.mimeType))
    }

    fun parseFromString(content: String?) {

        if (content == null) {
            importState.onNext(ImportState.Error(IllegalStateException("Content is null!")))
            return
        }

        if (selectedImporter != null) {
            importRepository.parse(selectedImporter!!, content)
                .map(ImportState::Parsed)
                .doOnError { throwable ->
                    importState.onNext(ImportState.Error(throwable))
                }
                .subscribe(importState::onNext, ExceptionHandlers::defaultExceptionHandler)
                .addTo(compositeDisposable)
        } else {
            importState.onNext(ImportState.Error(IllegalStateException("No importer selected")))
        }
    }

    fun import() {

        importRepository.import()
            .subscribe({
                importState.onNext(ImportState.Imported)
            }, { throwable ->
                importState.onNext(ImportState.Error(throwable))
            })
            .addTo(compositeDisposable)
    }

    fun confirmImport() {
        importState.onNext(ImportState.Idle)
    }
}
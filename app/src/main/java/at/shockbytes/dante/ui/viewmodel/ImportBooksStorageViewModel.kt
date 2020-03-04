package at.shockbytes.dante.ui.viewmodel

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import at.shockbytes.dante.importer.ImportRepository
import at.shockbytes.dante.importer.ImportStats
import at.shockbytes.dante.importer.Importer
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class ImportBooksStorageViewModel @Inject constructor(
    private val importRepository: ImportRepository,
    private val tracker: Tracker
) : BaseViewModel() {

    sealed class ImportState {

        object Idle : ImportState()

        data class AskForFile(val mimeType: String) : ImportState()

        data class Parsed(
            @StringRes val providerRes: Int,
            @DrawableRes val providerIconRes: Int,
            val importStats: ImportStats
        ) : ImportState()

        data class Error(val throwable: Throwable) : ImportState()

        object Imported : ImportState()
    }

    private var importState: ImportState = ImportState.Idle
        set(value) {
            field = value
            importSubject.onNext(value)
        }

    private val importSubject = PublishSubject.create<ImportState>()
    fun getImportState(): Observable<ImportState> = importSubject

    private var selectedImporter: Importer? = null

    init {
        reset()
    }

    fun reset() {
        importState = ImportState.Idle
    }

    fun startImport(importer: Importer) {
        selectedImporter = importer
        importState = ImportState.AskForFile(importer.mimeType)
        tracker.track(DanteTrackingEvent.StartImport(importer.name))
    }

    fun parseFromString(content: String?) {

        if (content == null) {
            importState = ImportState.Error(IllegalStateException("Content is null!"))
            return
        }

        if (selectedImporter != null) {
            importRepository.parse(selectedImporter!!, content)
                .map { stats ->
                    ImportState.Parsed(selectedImporter!!.title, selectedImporter!!.icon, stats)
                }
                .doOnError { throwable ->
                    importState = ImportState.Error(throwable)
                }
                .subscribe({ parsedState ->
                    importState = parsedState
                }, { throwable ->
                    Timber.e(throwable)
                })
                .addTo(compositeDisposable)
        } else {
            importState = ImportState.Error(IllegalStateException("No importer selected"))
        }
    }

    fun import() {

        importRepository.import()
            .subscribe({
                importState = ImportState.Imported
            }, { throwable ->
                importState = ImportState.Error(throwable)
            })
            .addTo(compositeDisposable)
    }

    fun confirmImport() {
        importState = ImportState.Idle
    }
}
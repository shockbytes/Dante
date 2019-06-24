package at.shockbytes.dante.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.BackupRepository
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.backup.model.RestoreStrategy
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    10.11.2018
 */
class BackupViewModel @Inject constructor(
    private val bookDao: BookEntityDao,
    private val backupRepository: BackupRepository,
    private val schedulers: SchedulerFacade
) : BaseViewModel() {

    private val loadBackupState = MutableLiveData<LoadBackupState>()
    fun getBackupState(): LiveData<LoadBackupState> = loadBackupState

    private val lastBackupTime = MutableLiveData<String>()
    fun getLastBackupTime(): LiveData<String> = lastBackupTime

    private val activeBackupStorageProviders = MutableLiveData<List<BackupStorageProvider>>()
    fun getActiveBackupProviders(): LiveData<List<BackupStorageProvider>> = activeBackupStorageProviders

    val makeBackupEvent = PublishSubject.create<State>()
    val deleteBackupEvent = PublishSubject.create<DeleteBackupState>()
    val applyBackupEvent = PublishSubject.create<ApplyBackupState>()
    val errorSubject = PublishSubject.create<Throwable>()

    fun connect(activity: FragmentActivity, forceReload: Boolean = false) {
        backupRepository.initialize(activity, forceReload)
            .subscribe({
                loadBackupState()
                updateLastBackupTime()
            }, { throwable ->
                Timber.e(throwable)
                errorSubject.onNext(throwable)
                loadBackupState.postValue(LoadBackupState.Error(throwable))
            })
            .addTo(compositeDisposable)

        postActiveBackupProviders()
    }

    fun disconnect() {
        backupRepository.close()
    }

    fun applyBackup(t: BackupMetadata, strategy: RestoreStrategy) {
        backupRepository.restoreBackup(t, bookDao, strategy)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
            .subscribe({
                val formattedTimestamp = DanteUtils.formatTimestamp(t.timestamp)
                applyBackupEvent.onNext(ApplyBackupState.Success(formattedTimestamp))
            }) { throwable ->
                Timber.e(throwable)
                applyBackupEvent.onNext(ApplyBackupState.Error(throwable))
            }
            .addTo(compositeDisposable)
    }

    fun makeBackup(backupStorageProvider: BackupStorageProvider) {
        backupRepository.backup(bookDao.bookObservable.blockingFirst(listOf()), backupStorageProvider)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
            .subscribe({
                updateLastBackupTime()
                loadBackupState()
                makeBackupEvent.onNext(State.Success(switchToBackupTab = true))
            }) { throwable ->
                Timber.e(throwable)
                makeBackupEvent.onNext(State.Error(throwable))
            }
            .addTo(compositeDisposable)
    }

    fun deleteItem(t: BackupMetadata, position: Int, currentItems: Int) {
        backupRepository.removeBackupEntry(t)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
            .subscribe({
                val wasLastEntry = (currentItems - 1) == 0
                deleteBackupEvent.onNext(DeleteBackupState.Success(position, wasLastEntry))

                if (wasLastEntry) {
                    updateLastBackupTime(true)
                }
            }) { throwable ->
                Timber.e(throwable)
                deleteBackupEvent.onNext(DeleteBackupState.Error(throwable))
            }
            .addTo(compositeDisposable)
    }

    private fun loadBackupState() {

        // First show loading screen
        loadBackupState.postValue(LoadBackupState.Loading)
        backupRepository.getBackups()
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.ui)
            .subscribe({ backupEntries ->

            // Check if backups are empty. One could argue that we can evaluate this in the fragment,
            // this solution seems cleaner, because it doesn't bother the view with even the simplest logic
            if (backupEntries.isNotEmpty()) {
                loadBackupState.value = LoadBackupState.Success(backupEntries)
            } else {
                loadBackupState.value = LoadBackupState.Empty
            }
        }) { throwable ->
            Timber.e(throwable)
            loadBackupState.postValue(LoadBackupState.Error(throwable))
        }.addTo(compositeDisposable)
    }

    private fun updateLastBackupTime(resetValue: Boolean = false) {

        // Reset the value if the last item was dismissed
        if (resetValue) {
            backupRepository.lastBackupTime = 0
        }

        val lastBackupMillis = backupRepository.lastBackupTime
        val lastBackup = if (lastBackupMillis > 0)
            DanteUtils.formatTimestamp(lastBackupMillis)
        else "---"
        lastBackupTime.postValue(lastBackup)
    }

    private fun postActiveBackupProviders() {
        val providers = backupRepository.backupProvider
            .filter { it.isEnabled }
            .map { it.backupStorageProvider }

        activeBackupStorageProviders.postValue(providers)
    }

    // -------------------------- State classes --------------------------

    sealed class LoadBackupState {
        data class Success(val backups: List<BackupMetadataState>) : LoadBackupState()
        object Empty : LoadBackupState()
        object Loading : LoadBackupState()
        data class Error(val throwable: Throwable) : LoadBackupState()
    }

    sealed class DeleteBackupState {
        data class Success(val deleteIndex: Int, val isBackupListEmpty: Boolean) : DeleteBackupState()
        data class Error(val throwable: Throwable) : DeleteBackupState()
    }

    sealed class ApplyBackupState {
        data class Success(val msg: String) : ApplyBackupState()
        data class Error(val throwable: Throwable) : ApplyBackupState()
    }

    sealed class State {
        data class Success(val switchToBackupTab: Boolean) : State()
        data class Error(val throwable: Throwable) : State()
    }
}
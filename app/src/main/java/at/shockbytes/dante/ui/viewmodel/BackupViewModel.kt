package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.backup.BackupEntry
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.tracking.Tracker
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * TODO Issues:
 * - Show empty view when user dismisses last item
 * - Disable swipe to dismiss
 */
class BackupViewModel @Inject constructor(
        private val bookDao: BookEntityDao,
        private val backupManager: BackupManager,
        private val tracker: Tracker) : BaseViewModel() {

    private val loadBackupState = MutableLiveData<LoadBackupState>()
    private val lastBackupTime = MutableLiveData<String>()

    val makeBackupEvent = PublishSubject.create<State>()
    val deleteBackupEvent = PublishSubject.create<DeleteBackupState>()
    val applyBackupEvent = PublishSubject.create<ApplyBackupState>()

    init {
        poke()
    }

    override fun poke() {
        loadBackupState()
        updateLastBackupTime()
    }

    fun getBackupState(): LiveData<LoadBackupState> = loadBackupState
    fun getLastBackupTime(): LiveData<String> = lastBackupTime


    fun applyBackup(t: BackupEntry, strategy: BackupManager.RestoreStrategy) {
        backupManager.restoreBackup(t, bookDao, strategy)
                .subscribe({
                    val formattedTimestamp = DanteUtils.formatTimestamp(t.timestamp)
                    applyBackupEvent.onNext(ApplyBackupState.Success(formattedTimestamp))
                    tracker.trackOnBackupRestored()
                }) { throwable ->
                    Timber.e(throwable)
                    applyBackupEvent.onNext(ApplyBackupState.Error(throwable))
                }.addTo(compositeDisposable)
    }

    fun makeBackup() {
        backupManager.backup(bookDao.bookObservable).subscribe({
            updateLastBackupTime()
            loadBackupState()
            makeBackupEvent.onNext(State.Success)
            tracker.trackOnBackupMade()
        }) { throwable ->
            Timber.e(throwable)
            makeBackupEvent.onNext(State.Error(throwable))
        }.addTo(compositeDisposable)
    }

    fun deleteItem(t: BackupEntry, position: Int) {
        backupManager.removeBackupEntry(t)
                .subscribe({
                    deleteBackupEvent.onNext(DeleteBackupState.Success(position))
                }) { throwable ->
                    Timber.e(throwable)
                    deleteBackupEvent.onNext(DeleteBackupState.Error(throwable))
                }.addTo(compositeDisposable)
    }

    private fun loadBackupState() {

        // First show loading screen
        loadBackupState.postValue(LoadBackupState.Loading)
        backupManager.backupList.subscribe({ backupEntries ->

            // Check if backups are empty. One could argue that we can evaluate this in the fragment,
            // this solution seems cleaner, because it doesn't bother the view with even the simplest logic
            if (backupEntries.isNotEmpty()) {
                loadBackupState.postValue(LoadBackupState.Success(backupEntries))
            } else {
                loadBackupState.postValue(LoadBackupState.Empty)
            }
        }) { throwable ->
            Timber.e(throwable)
            loadBackupState.postValue(LoadBackupState.Error(throwable))
        }.addTo(compositeDisposable)
    }

    private fun updateLastBackupTime() {
        val lastBackupMillis = backupManager.lastBackupTime
        val lastBackup = if (lastBackupMillis > 0)
            DanteUtils.formatTimestamp(lastBackupMillis)
        else "---"
        lastBackupTime.postValue(lastBackup)
    }

    // -------------------------- State classes --------------------------

    sealed class LoadBackupState {
        data class Success(val backups: List<BackupEntry>) : LoadBackupState()
        object Empty : LoadBackupState()
        object Loading : LoadBackupState()
        data class Error(val throwable: Throwable) : LoadBackupState()
    }

    sealed class DeleteBackupState {
        data class Success(val deleteIndex: Int) : DeleteBackupState()
        data class Error(val throwable: Throwable) : DeleteBackupState()
    }

    sealed class ApplyBackupState {
        data class Success(val msg: String) : ApplyBackupState()
        data class Error(val throwable: Throwable) : ApplyBackupState()
    }

    sealed class State {
        object Success : State()
        data class Error(val throwable: Throwable) : State()
    }
}
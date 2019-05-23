package at.shockbytes.dante.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import at.shockbytes.dante.ui.viewmodel.BookDetailViewModel
import at.shockbytes.dante.ui.viewmodel.BookListViewModel
import at.shockbytes.dante.ui.viewmodel.FeatureFlagConfigViewModel
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.ui.viewmodel.ManualAddViewModel
import at.shockbytes.dante.ui.viewmodel.SearchViewModel
import at.shockbytes.dante.ui.viewmodel.StatisticsViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
@Suppress("UNCHECKED_CAST")
@Singleton
class ViewModelFactory @Inject constructor(
    private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModels[modelClass]?.get() as T
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun mainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookListViewModel::class)
    internal abstract fun bookListViewModel(viewModel: BookListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookDetailViewModel::class)
    internal abstract fun bookDetailViewModel(viewModel: BookDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManualAddViewModel::class)
    internal abstract fun manualAddViewModel(viewModel: ManualAddViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatisticsViewModel::class)
    internal abstract fun statisticsViewModel(viewModel: StatisticsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BackupViewModel::class)
    internal abstract fun backupViewModel(viewModel: BackupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    internal abstract fun searchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeatureFlagConfigViewModel::class)
    internal abstract fun featureFlagConfigViewModel(viewModel: FeatureFlagConfigViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun loginViewModel(viewModel: LoginViewModel): ViewModel
}
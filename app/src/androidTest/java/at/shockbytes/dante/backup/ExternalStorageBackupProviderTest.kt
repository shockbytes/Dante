package at.shockbytes.dante.backup

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.provider.external.ExternalStorageBackupProvider
import at.shockbytes.dante.storage.ExternalStorageInteractor
import at.shockbytes.dante.util.scheduler.TestSchedulerFacade
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import at.shockbytes.dante.ui.activity.MainActivity
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`


/**
 * Author:  Martin Macheiner
 * Date:    11.06.2019
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class ExternalStorageBackupProviderTest {

    private lateinit var backupProvider: ExternalStorageBackupProvider

    private val externalStorageInteractor = mock(ExternalStorageInteractor::class.java)

    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        backupProvider = ExternalStorageBackupProvider(schedulerFacade, gson, externalStorageInteractor)
    }

    @Test
    fun test_initialize_without_context() {

        backupProvider
            .initialize(null)
            .test()
            .assertError(BackupServiceConnectionException::class.java)

        assertThat(backupProvider.isEnabled).isFalse()
    }

    @Test
    fun test_initialize_without_creating_base_dir() {

        `when`(externalStorageInteractor.createBaseDirectory("Dante"))
            .thenThrow(IllegalStateException::class.java)

        backupProvider
            .initialize(activityRule.activity)
            .test()
            .assertError(IllegalStateException::class.java)

        assertThat(backupProvider.isEnabled).isFalse()
    }

    @Test
    fun test_initialize_working() {

        backupProvider
            .initialize(activityRule.activity)
            .test()
            .assertComplete()

        assertThat(backupProvider.isEnabled).isTrue()
    }


    companion object {

        private val schedulerFacade = TestSchedulerFacade()
        private val gson = Gson()
    }

}
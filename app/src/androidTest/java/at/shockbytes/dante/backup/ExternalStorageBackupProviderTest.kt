package at.shockbytes.dante.backup

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import at.shockbytes.dante.backup.model.BackupServiceConnectionException
import at.shockbytes.dante.backup.provider.external.ExternalStorageBackupProvider
import at.shockbytes.dante.storage.ExternalStorageInteractor
import at.shockbytes.dante.util.scheduler.TestSchedulerFacade
import at.shockbytes.dante.util.fromJson
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import at.shockbytes.dante.ui.activity.MainActivity
import androidx.test.rule.ActivityTestRule
import at.shockbytes.dante.backup.model.BackupItem
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.util.permission.TestPermissionManager
import at.shockbytes.test.ObjectCreator
import at.shockbytes.test.TestResourceManager
import at.shockbytes.test.any
import io.reactivex.Single
import org.junit.Rule
import org.mockito.Mockito.`when`
import java.io.File

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
        backupProvider = ExternalStorageBackupProvider(
            schedulerFacade,
            gson,
            externalStorageInteractor,
            permissionManager
        )
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

    @Test
    fun test_backup_with_empty_list() {

        val books = listOf<BookEntity>()

        backupProvider.backup(books)
            .test()
            .assertComplete()
    }

    @Test
    fun test_backup_with_populated_list() {

        val books = ObjectCreator.getPopulatedListOfBookEntities()

        backupProvider.backup(books)
            .test()
            .assertComplete()
    }

    @Test
    fun test_backup_with_external_storage_error() {

        `when`(externalStorageInteractor.writeToFileInDirectory(any(), any(), any()))
            .thenThrow(IllegalStateException::class.java)

        val books = ObjectCreator.getPopulatedListOfBookEntities()

        backupProvider.backup(books)
            .test()
            .assertNotComplete()
            .assertError(IllegalStateException::class.java)
    }

    @Test
    fun test_getBackupEntries() {

        val file1 = File("entry_1.dbi")
        val file2 = File("entry_2.dbi")

        val metadata = gson.fromJson<List<BackupMetadata>>(TestResourceManager.getTestResourceAsString(javaClass, "/backup_entries.json"))
        val expected = metadata.map { BackupMetadataState.Active(it) }

        val backupItem1 = BackupItem(metadata[0], ObjectCreator.getPopulatedListOfBookEntities())
        val backupItem2 = BackupItem(metadata[1], ObjectCreator.getPopulatedListOfBookEntities().subList(0, 1))

        `when`(externalStorageInteractor.listFilesInDirectory(any(), any()))
            .thenReturn(Single.just(listOf(file1, file2)))

        `when`(externalStorageInteractor.readFileContent("Dante", file1.name))
            .thenReturn(gson.toJson(backupItem1))

        `when`(externalStorageInteractor.readFileContent("Dante", file2.name))
            .thenReturn(gson.toJson(backupItem2))

        backupProvider.getBackupEntries()
            .test()
            .assertValue { states ->
                states == expected
            }
    }

    @Test
    fun test_getBackupEntries_no_entries() {

        `when`(externalStorageInteractor.listFilesInDirectory(any(), any()))
            .thenReturn(Single.just(listOf()))

        backupProvider.getBackupEntries()
            .test()
            .assertValue { states -> states == listOf<File>() }
            .assertComplete()
    }

    @Test
    fun test_getBackupEntries_backup_file_corrupted() {

        val file1 = File("entry_1.dbi")
        val file2 = File("entry_2.dbi")

        val metadata = gson.fromJson<List<BackupMetadata>>(TestResourceManager.getTestResourceAsString(javaClass, "/backup_entries.json"))
        val expected = listOf(metadata.map { BackupMetadataState.Active(it) }.first())

        val backupItem1 = BackupItem(metadata[0], ObjectCreator.getPopulatedListOfBookEntities())
        val backupItem2 = BackupItem(metadata[1], ObjectCreator.getPopulatedListOfBookEntities().subList(0, 1))

        val corruptJson2 = gson.toJson(backupItem2).lineSequence().drop(1).joinToString("")

        `when`(externalStorageInteractor.listFilesInDirectory(any(), any()))
            .thenReturn(Single.just(listOf(file1, file2)))

        `when`(externalStorageInteractor.readFileContent("Dante", file1.name))
            .thenReturn(gson.toJson(backupItem1))

        `when`(externalStorageInteractor.readFileContent("Dante", file2.name))
            .thenReturn(corruptJson2)

        backupProvider.getBackupEntries()
            .test()
            .assertValue { states ->
                states == expected
            }
    }

    @Test
    fun test_mapEntryToBooks() {

        val metadata = BackupMetadata(
            id = "12345",
            device = "Nokia 7.1",
            storageProvider = BackupStorageProvider.EXTERNAL_STORAGE,
            books = 3,
            timestamp = System.currentTimeMillis(),
            fileName = "test_mapEntryToBooks${BackupRepository.BACKUP_ITEM_SUFFIX}"
        )

        val expected = ObjectCreator.getPopulatedListOfBookEntities()
        val backupItem = BackupItem(metadata, expected)

        `when`(externalStorageInteractor.readFileContent("Dante", metadata.fileName))
            .thenReturn(gson.toJson(backupItem))

        backupProvider.mapEntryToBooks(metadata)
            .test()
            .assertValue(expected)
    }

    @Test
    fun test_mapEntryToBooks_corrupt_json() {

        val metadata = BackupMetadata(
            id = "12345",
            device = "Nokia 7.1",
            storageProvider = BackupStorageProvider.EXTERNAL_STORAGE,
            books = 3,
            timestamp = System.currentTimeMillis(),
            fileName = "test_mapEntryToBooks${BackupRepository.BACKUP_ITEM_SUFFIX}"
        )

        val expected = ObjectCreator.getPopulatedListOfBookEntities()
        val backupItem = BackupItem(metadata, expected)
        val corruptJson = gson.toJson(backupItem).lineSequence().drop(1).joinToString()

        `when`(externalStorageInteractor.readFileContent("Dante", metadata.fileName))
            .thenReturn(corruptJson)

        backupProvider.mapEntryToBooks(metadata)
            .test()
            .assertError(NullPointerException::class.java)
    }

    companion object {

        private val schedulerFacade = TestSchedulerFacade()
        private val gson = Gson()
        private val permissionManager = TestPermissionManager()
    }
}
package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest : KoinTest {

    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val listReminders = arrayOf(
        ReminderDTO("title", "description",
            "location", 0.0, 0.0),
        ReminderDTO("title", "description",
            "location", 0.0, 0.0)
    )

    @Before
    fun init() {
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_success_remindersObtainedFromDataSource() = runBlockingTest {
        dataSource.successFetching = true
        dataSource.listReminders.addAll(listReminders)
        viewModel.loadReminders()
        val remindersList = viewModel.remindersList.getOrAwaitValue()
        val result = remindersList == (dataSource.getReminders() as Result.Success).data.map {
            ReminderDataItem(it.title, it.description, it.location, it.latitude, it.longitude, it.id)
        }
        assertEquals(true, result)
    }

    @Test
    fun loadReminders_error_showSnackbarWithErrorMessage() = runBlockingTest {
        dataSource.successFetching = false
        dataSource.listReminders.addAll(listReminders)
        viewModel.loadReminders()
        val result = viewModel.showSnackBar.getOrAwaitValue().isNotEmpty()
        assertEquals(true, result)
    }

    @Test
    fun deleteAllReminders_allRemindersDeleted() = runBlockingTest {
        dataSource.listReminders.addAll(listReminders)
        viewModel.deleteAllReminders()
        val result = dataSource.listReminders.isEmpty()
        assertEquals(true, result)
    }

    @Test
    fun loadReminders_emptyList_showNoDataTrue() = runBlockingTest {
        viewModel.loadReminders()
        val result = viewModel.showNoData.getOrAwaitValue()
        assertEquals(true, result)
    }

    @Test
    fun loadReminders_notEmptyList_showNoDataFalse() = runBlockingTest {
        dataSource.listReminders.addAll(listReminders)
        viewModel.loadReminders()
        val result = viewModel.showNoData.getOrAwaitValue()
        assertEquals(false, result)
    }

    @Test
    fun deleteAllReminders_showNoDataTrue() = runBlockingTest {
        viewModel.deleteAllReminders()
        val result = viewModel.showNoData.getOrAwaitValue()
        assertEquals(true, result)
    }

}
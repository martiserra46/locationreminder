package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest : KoinTest {

    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val validReminder = ReminderDataItem("title", "description",
        "location", 0.0, 0.0)

    private val reminderWithNullTitle = ReminderDataItem(null, "description",
        "location", 0.0, 0.0)

    private val reminderWithNullLocation = ReminderDataItem("title", "description",
        null, 0.0, 0.0)

    @Before
    fun init() {
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun onClear_liveDataObjectsCleared() {
        viewModel.onClear()
        val title = viewModel.reminderTitle.getOrAwaitValue()
        val description = viewModel.reminderDescription.getOrAwaitValue()
        val location = viewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val poi = viewModel.selectedPOI.getOrAwaitValue()
        val latitude = viewModel.latitude.getOrAwaitValue()
        val longitude = viewModel.longitude.getOrAwaitValue()

        val result = title == null && description == null && location == null && poi == null
                && latitude == null && longitude == null
        assertEquals(true, result)
    }

    @Test
    fun validateAndSaveReminder_validReminder_reminderSaved() = runBlockingTest {
        viewModel.validateAndSaveReminder(validReminder)
        val result = dataSourceContainsReminder(validReminder)
        assertEquals(true, result)
    }

    @Test
    fun validateAndSaveReminder_validReminder_navigateBack() = runBlockingTest {
        viewModel.validateAndSaveReminder(validReminder)
        val result = viewModel.navigationCommand.getOrAwaitValue() is NavigationCommand.Back
        assertEquals(true, result)
    }

    @Test
    fun validateAndSaveReminder_validReminder_showToastMessage() = runBlockingTest {
        viewModel.validateAndSaveReminder(validReminder)
        val result = viewModel.showToast.getOrAwaitValue().isNotEmpty()
        assertEquals(true, result)
    }

    @Test
    fun validateAndSaveReminder_validReminder_showLoading() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        viewModel.validateAndSaveReminder(validReminder)
        var result = viewModel.showLoading.getOrAwaitValue()
        assertEquals(true, result)
        mainCoroutineRule.resumeDispatcher()
        result = viewModel.showLoading.getOrAwaitValue()
        assertEquals(false, result)
    }

    @Test
    fun validateAndSaveReminder_reminderWithNullTitle_reminderNotSaved() = runBlockingTest {
        viewModel.validateAndSaveReminder(reminderWithNullTitle)
        val result = dataSourceContainsReminder(reminderWithNullTitle).not()
        assertEquals(true, result)
    }

    @Test
    fun validateAndSaveReminder_reminderWithNullLocation_reminderNotSaved() = runBlockingTest {
        viewModel.validateAndSaveReminder(reminderWithNullLocation)
        val result = dataSourceContainsReminder(reminderWithNullLocation).not()
        assertEquals(true, result)
    }

    private fun dataSourceContainsReminder(reminderDataItem: ReminderDataItem) : Boolean {
        return dataSource.listReminders.firstOrNull {
            it.id == validReminder.id &&
                    it.title == reminderDataItem.title &&
                    it.description == reminderDataItem.description &&
                    it.location == reminderDataItem.location &&
                    it.latitude == reminderDataItem.latitude &&
                    it.longitude == reminderDataItem.longitude
        } != null
    }

}
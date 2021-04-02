package com.eselman.locationreminderapp.locationreminders.savereminder

import MainCoroutineRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eselman.locationreminderapp.R
import com.eselman.locationreminderapp.locationreminders.data.local.FakeDataSource
import com.eselman.locationreminderapp.locationreminders.getOrAwaitValue
import com.eselman.locationreminderapp.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers
import org.junit.*
import org.junit.Assert.assertThat
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @After
    fun finish() {
        stopKoin()
    }

    @Test
    fun saveReminderTest() {
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(false))
        val reminder = ReminderDataItem("reminder1", "reminder description", "location",0.0,0.0)
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), Matchers.`is`("Reminder Saved !"))
    }

    @Test
    fun loadRemindersCheckLoadingTest() {
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(false))
        val reminder = ReminderDataItem("reminder1", "reminder description", "location",0.0,0.0)

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(false))
    }

    @Test
    fun saveReminderShouldReturnErrorTest() {
        val dataSource = FakeDataSource(false)
        dataSource.setReturnError(true)
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        val reminder = ReminderDataItem("", "reminder description", "location",0.0,0.0)
        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), Matchers.`is`(R.string.err_enter_title))
    }
}
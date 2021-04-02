package com.eselman.locationreminderapp.locationreminders.reminderslist

import MainCoroutineRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eselman.locationreminderapp.locationreminders.data.local.FakeDataSource
import com.eselman.locationreminderapp.locationreminders.getOrAwaitValue
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    @After
    fun finish() {
        stopKoin()
    }

    @Test
    fun loadRemindersTestSuccess() {
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(false))
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(3))
    }


    @Test
    fun loadRemindersNoData() {
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(true))
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(0))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadRemindersCheckLoading() {
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(false))

        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadRemindersShouldReturnError() {
        val dataSource = FakeDataSource(false)
        dataSource.setReturnError(true)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Error getting reminders"))
    }
}
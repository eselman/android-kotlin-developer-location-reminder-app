package com.eselman.locationreminderapp.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eselman.locationreminderapp.locationreminders.data.ReminderDataSource
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
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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
}
package com.eselman.locationreminderapp.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.eselman.locationreminderapp.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.eselman.locationreminderapp.locationreminders.data.dto.Result
import org.junit.After
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var repository: FakeDataSource

    @Before
    fun setup() {
        repository = FakeDataSource(false)
    }

    @After
    fun finish() {
        stopKoin()
    }

    @Test
    fun getRemindersTest() = runBlocking {
        val result = repository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(result, notNullValue())
        assertThat(result.data.size, `is`(3))
    }

    @Test
    fun getReminderTest() = runBlocking {
        val result = repository.getReminder("1111") as Result.Success<ReminderDTO>
        assertThat(result, notNullValue())
        assertThat(result.data.id, `is`("1111"))
    }

    @Test
    fun saveReminderTest() = runBlocking {
        repository.saveReminder(ReminderDTO("reminder4", "reminder description", "location", 0.0, 0.0, "4444"))

        val remindersResult = repository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(remindersResult, notNullValue())
        assertThat(remindersResult.data.size, `is`(4))

        val reminderResult = repository.getReminder("4444") as Result.Success<ReminderDTO>
        assertThat(reminderResult, notNullValue())
        assertThat(reminderResult.data.id, `is`("4444"))
    }
}
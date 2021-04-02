package com.eselman.locationreminderapp.locationreminders.data.local

import com.eselman.locationreminderapp.locationreminders.data.ReminderDataSource
import com.eselman.locationreminderapp.locationreminders.data.dto.ReminderDTO
import com.eselman.locationreminderapp.locationreminders.data.dto.Result


//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val emptyData: Boolean) : ReminderDataSource {
    private var shouldReturnError = false

    private val reminders = mutableListOf(
            ReminderDTO("reminder1", "reminder description", "location",0.0,0.0, "1111"),
            ReminderDTO("reminder2", "reminder description", "location",0.0,0.0, "2222"),
            ReminderDTO("reminder3", "reminder description", "location",0.0,0.0, "3333")

    )

    private val emptyReminders = mutableListOf<ReminderDTO>()


    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) {
            Result.Error("Error getting reminders")
        } else {
            if (emptyData) Result.Success(emptyReminders) else Result.Success(reminders)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        if (!shouldReturnError) {
            reminders.add(reminder)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldReturnError) Result.Error("Error getting reminder") else Result.Success(reminders.first { it.id == id })
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}
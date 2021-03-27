package com.eselman.locationreminderapp.locationreminders.data.local

import com.eselman.locationreminderapp.locationreminders.data.ReminderDataSource
import com.eselman.locationreminderapp.locationreminders.data.dto.ReminderDTO
import com.eselman.locationreminderapp.locationreminders.data.dto.Result


//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val emptyData: Boolean) : ReminderDataSource {


    private val reminders = mutableListOf(
            ReminderDTO("reminder1", "reminder description", "location",0.0,0.0, "1111"),
            ReminderDTO("reminder2", "reminder description", "location",0.0,0.0, "2222"),
            ReminderDTO("reminder3", "reminder description", "location",0.0,0.0, "3333")

    )

    private val emptyReminders = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (emptyData) Result.Success(emptyReminders) else Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
          val reminder = reminders.first { it.id == id }
           return Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}
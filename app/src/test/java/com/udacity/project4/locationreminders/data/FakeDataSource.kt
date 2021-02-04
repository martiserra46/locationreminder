package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var successFetching = true

    val listReminders = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (successFetching) Result.Success(listReminders) else Result.Error("Error message")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        listReminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (!successFetching) return Result.Error("")
        val result = listReminders.firstOrNull { it.id == id }
        return if (result != null) {
            Result.Success(result)
        } else {
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        listReminders.clear()
    }
}
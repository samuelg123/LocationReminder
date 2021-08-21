package com.udacity.project4.locationreminders.data

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.util.LinkedHashMap

//Use FakeDataSource that acts as a test double to the LocalDataSource
class ReminderFakeRepository : ReminderDataSource {

    private var reminderServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(reminderServiceData.values.toList())
    }

    fun addReminders(vararg reminder: ReminderDTO) {
        reminderServiceData.putAll(reminder.map { Pair(it.id, it) })
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminderServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find task")
    }

    override suspend fun deleteAllReminders() {
        reminderServiceData.clear()
    }


}
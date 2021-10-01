package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.util.*

//Use FakeDataSource that acts as a test double to the LocalDataSource
class ReminderFakeDataSource : ReminderDataSource {
    private var shouldReturnError = false
    private var reminderServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return Result.Success(reminderServiceData.values.toList())
    }

    fun addReminders(vararg reminder: ReminderDTO) {
        reminderServiceData.putAll(reminder.map { Pair(it.id, it) })
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        reminderServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find reminder")
    }

    override suspend fun deleteAllReminders() {
        reminderServiceData.clear()
    }

}
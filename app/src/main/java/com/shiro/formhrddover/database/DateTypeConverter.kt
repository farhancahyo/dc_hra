package com.shiro.formhrddover.database

import androidx.room.TypeConverter
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class DateTypeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun stringToTimestamp(string: String): Long {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val parsedDate: Date = dateFormat.parse(string)
        val timestamp = Timestamp(parsedDate.time)
        return timestamp.time
    }

}
package com.company.arminro.qrkatalog.helpers

import androidx.room.TypeConverter
import java.util.*

// based on:
// https://stackoverflow.com/questions/51438926/typeconverter-has-private-access-in-typeconverter-error-with-room-in-android
// https://stackoverflow.com/questions/50313525/room-using-date-field

object TimeConverters {
    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
        return (if (date == null) null else date!!.time)?.toLong()
    }
}
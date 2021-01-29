package dev.kiwik.tracking.domain.db.converters

import androidx.room.TypeConverter
import org.joda.time.DateTime

class DateMilisConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): DateTime? {
        return value?.let { DateTime(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: DateTime?): Long? {
        return date?.millis
    }
}
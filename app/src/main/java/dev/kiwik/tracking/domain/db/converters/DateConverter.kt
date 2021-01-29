package dev.kiwik.tracking.domain.db.converters

import androidx.room.TypeConverter
import dev.kiwik.tracking.utilities.DateUtil
import org.joda.time.DateTime

class DateConverter {
    private val FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    @TypeConverter
    fun fromTimestamp(value: String?): DateTime? {
        return if (value == null) null else DateUtil.parseDate(FORMAT, value)
    }

    @TypeConverter
    fun dateToTimestamp(date: DateTime?): String? {
        return date?.toString(FORMAT)
    }
}
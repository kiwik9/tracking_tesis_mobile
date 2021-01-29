package dev.kiwik.tracking.utilities

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object DateUtil {

    fun parseDate(format: String, date: String): DateTime {
        return DateTimeFormat.forPattern(format).parseDateTime(date)
    }
}
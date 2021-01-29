package dev.kiwik.tracking.utilities

import com.squareup.moshi.*
import org.joda.time.DateTime

class CustomDateTimeAdapter : JsonAdapter<DateTime>() {
    @FromJson
    override fun fromJson(reader: JsonReader): DateTime? {
        return try {
            val dateAsString = reader.nextString()
            DateUtil.parseDate(SERVER_FORMAT, dateAsString)
        } catch (e: Exception) {
            reader.nextNull()
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: DateTime?) {
        if (value != null) {
            writer.value(value.toString(SERVER_FORMAT))
        }
    }

    companion object {
        const val SERVER_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    }
}
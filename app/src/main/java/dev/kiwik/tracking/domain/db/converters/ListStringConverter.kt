package dev.kiwik.tracking.domain.db.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import dev.kiwik.tracking.utilities.MoshiUtil
import java.lang.reflect.ParameterizedType

class ListStringConverter {
    private val listMyData: ParameterizedType =
        Types.newParameterizedType(List::class.java, String::class.java)
    val adapter: JsonAdapter<List<String>> = MoshiUtil.instance.adapter<List<String>>(listMyData)

    @TypeConverter
    fun fromJsonList(value: String?): List<String>? {
        return if (value == null) null else adapter.fromJson(value)
    }

    @TypeConverter
    fun listToJson(value: List<String>?): String? {
        return adapter.toJson(value)
    }
}
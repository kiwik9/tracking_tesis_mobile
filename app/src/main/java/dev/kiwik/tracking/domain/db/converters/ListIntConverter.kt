package dev.kiwik.tracking.domain.db.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import dev.kiwik.tracking.utilities.MoshiUtil
import java.lang.reflect.ParameterizedType

class ListIntConverter {
    private val listMyData: ParameterizedType =
            Types.newParameterizedType(List::class.java, Int::class.java)
    val adapter: JsonAdapter<List<Int>> = MoshiUtil.instance.adapter<List<Int>>(listMyData)

    @TypeConverter
    fun fromJsonList(value: String?): List<Int>? {
        return if (value == null) null else adapter.fromJson(value)
    }

    @TypeConverter
    fun listToJson(value: List<Int>?): String? {
        return adapter.toJson(value)
    }
}
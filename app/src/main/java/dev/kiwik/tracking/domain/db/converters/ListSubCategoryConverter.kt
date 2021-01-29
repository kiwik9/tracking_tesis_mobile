package dev.kiwik.tracking.domain.db.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import dev.kiwik.tracking.domain.entities.SubCategory
import dev.kiwik.tracking.utilities.MoshiUtil
import java.lang.reflect.ParameterizedType

class ListSubCategoryConverter {
    private val listMyData: ParameterizedType =
        Types.newParameterizedType(List::class.java, SubCategory::class.java)
    val adapter: JsonAdapter<List<SubCategory>> =
        MoshiUtil.instance.adapter<List<SubCategory>>(listMyData)

    @TypeConverter
    fun fromJsonList(value: String?): List<SubCategory>? {
        return if (value == null) null else adapter.fromJson(value)
    }

    @TypeConverter
    fun listToJson(value: List<SubCategory>?): String? {
        return adapter.toJson(value)
    }
}
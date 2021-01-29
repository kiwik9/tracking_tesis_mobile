package dev.kiwik.tracking.domain.db.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import dev.kiwik.tracking.domain.entities.Geometry
import dev.kiwik.tracking.utilities.MoshiUtil
import java.lang.reflect.ParameterizedType

class GeometryConverter {

    private val myData: ParameterizedType = Types.newParameterizedType(Geometry::class.java)
    val adapter: JsonAdapter<Geometry> = MoshiUtil.instance.adapter<Geometry>(myData)

    @TypeConverter
    fun getGeometry(value: String): Geometry? {
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun getValue(geo: Geometry): String? {
        return adapter.toJson(geo)
    }
}
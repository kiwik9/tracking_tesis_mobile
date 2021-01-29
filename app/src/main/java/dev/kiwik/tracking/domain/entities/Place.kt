package dev.kiwik.tracking.domain.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.squareup.moshi.JsonClass
import dev.kiwik.tracking.domain.db.converters.GeometryConverter
import dev.kiwik.tracking.domain.db.converters.ListStringConverter


@JsonClass(generateAdapter = true)
@TypeConverters(ListStringConverter::class, GeometryConverter::class)
@Entity(tableName = "m_place", indices = [Index(value = ["place_id"], unique = true)])
data class Place(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val place_id: String,
        val name: String,
        val rating: Double? = 0.0,
        val user_ratings_total: Int? = 0,
        val types: List<String>? = mutableListOf(),
        val vicinity: String,
        val price_level: Int? = null,
        val icon: String?,
        val geometry: Geometry
)

data class Geometry(
        val location: LocationGoogle
)

data class LocationGoogle(
        val lat: Double,
        val lng: Double
)
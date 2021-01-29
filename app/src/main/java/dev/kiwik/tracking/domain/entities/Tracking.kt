package dev.kiwik.tracking.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.*


@JsonClass(generateAdapter = true)
@Entity(tableName = "m_tracking")
data class Tracking(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    var trackingId: String = UUID.randomUUID().toString(),
    val userId: Int,
    var latitude: Double,
    var longitude: Double,
    val altitude: Float,
    val speed: Float,
    var accuracy: Float,
    val bearing: Float,
    val interval: Int,
    val createdAt: String
)

class FilterParamTracking(
    val initDate: String = "",
    val endDate: String = "",
    val initPatternDate: String = "",
    val endPatternDate: String = ""
)

data class TrackingConfig(
    var radius: Int = 250,
    var startTime: Int = 1,
    var endTime: Int = 5
)
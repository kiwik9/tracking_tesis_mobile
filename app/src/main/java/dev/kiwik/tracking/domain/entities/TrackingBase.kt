package dev.kiwik.tracking.domain.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.joda.time.DateTime
import java.util.*


@JsonClass(generateAdapter = true)
@Entity(tableName = "m_tracking_base")
data class TrackingBase(
        @PrimaryKey(autoGenerate = true)
        val id: Int? = null,
        var trackingId: String = UUID.randomUUID().toString(),
        val userId : Int,
        var latitude: Double,
        var longitude: Double,
        val altitude: Float,
        val speed: Float,
        var accuracy: Float,
        val bearing: Float,
        val interval: Int,
        val createdAt: String
)

fun TrackingBase.toTracking(): Tracking {
        return Tracking(
                userId = userId,
                trackingId = this.trackingId,
                latitude = latitude,
                speed = speed,
                altitude = altitude,
                bearing = bearing,
                accuracy = accuracy,
                longitude = longitude,
                interval = interval,
                createdAt = createdAt
        )
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDefaultTracking(): Tracking {
        val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
        val interval = 5
        return  Tracking(
                userId = 0,
                latitude = 0.0,
                speed = 0f,
                altitude = 0f,
                bearing = 0f,
                accuracy = 999f,
                longitude = 0.0,
                interval = interval,
                createdAt = now
        )
}
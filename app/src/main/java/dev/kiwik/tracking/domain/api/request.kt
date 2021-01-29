package dev.kiwik.tracking.domain.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.kiwik.tracking.domain.entities.Tracking

@JsonClass(generateAdapter = true)
data class TrackingRequest(
        @Json(name = "gps_list")
        val gpsList: List<Tracking>
)

data class UpdateTokenRequest(
        val userId: Int,
        val token: String
)

data class LoginRequest(
        val email: String,
        val password: String
)

data class LoginGoogleRequest(
        val email: String,
        var phone: String,
        var name: String
)

data class GetTrackingRequest(
        val userId: Int,
        val initDate: String,
        val endDate: String
)

package dev.kiwik.tracking.preferences

import com.squareup.moshi.JsonClass
import dev.kiwik.tracking.domain.entities.FilterParamTracking
import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.domain.entities.TrackingConfig
import dev.kiwik.tracking.domain.entities.User

@JsonClass(generateAdapter = true)
class SessionData {
    var loggedUser: User? = null
    var currentLocations = mutableListOf<Tracking>()
    var filterParamTracking = FilterParamTracking()
    var lastRecommend: String? = null
    var lastLocation: Tracking? = null
    var userToAdd: User? = null
    var intentRegister = 0
    var trackingConfig = TrackingConfig()
}
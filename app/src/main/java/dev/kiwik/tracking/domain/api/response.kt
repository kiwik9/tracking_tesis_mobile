package dev.kiwik.tracking.domain.api

import com.squareup.moshi.JsonClass
import dev.kiwik.tracking.domain.entities.*


@JsonClass(generateAdapter = true)
data class ApiError(
        val title: String,
        val status: Int,
        val detail: String = "",
        val instance: String = ""
)

data class TrackingResponse(
        var id: Int
)

data class UpdateTokenResponse(
        val update: Boolean
)

data class LoginResponse(
        val user: User?,
        val error: String?
)

data class GetTrackingResponse(
        val list: List<Tracking>
)

data class CategoryResponse(
        val categories: List<Category>,
        val subCategories: List<SubCategory>
)

data class GooglePlaceResponse(
        val results: List<Place>,
        val status: String
)
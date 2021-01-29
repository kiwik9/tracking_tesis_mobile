package dev.kiwik.tracking.domain.api

import dev.kiwik.tracking.domain.entities.Tracking
import dev.kiwik.tracking.domain.entities.UserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url


interface IRestService {

    @POST("/api/insert-tracking")
    suspend fun insertTracking(@Body body: List<Tracking>): Response<TrackingResponse>

    @POST("/api/update-token")
    suspend fun updateFirebaseToken(@Body body: UpdateTokenRequest): Response<UpdateTokenResponse>

    @POST("/api/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("/api/login-google")
    suspend fun loginWithGoogle(@Body body: LoginGoogleRequest): LoginResponse

    @POST("/api/register")
    suspend fun register(@Body body: UserRequest): LoginResponse

    @POST("/api/update-user")
    suspend fun updateUser(@Body body: UserRequest): LoginResponse

    @POST("/api/get-tracking")
    suspend fun getTracking(@Body body: GetTrackingRequest): Response<GetTrackingResponse>

    @POST("/api/get-categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET
    suspend fun getPlaces(@Url url: String): Response<GooglePlaceResponse>
}
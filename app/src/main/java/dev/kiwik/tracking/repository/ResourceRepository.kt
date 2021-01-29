package dev.kiwik.tracking.repository

import dev.kiwik.tracking.domain.api.*
import dev.kiwik.tracking.domain.db.dao.ResourcesDao
import dev.kiwik.tracking.domain.entities.Place
import retrofit2.Response

class ResourceRepository(private val dao: ResourcesDao) {

    suspend fun updateFirebaseToken(request: UpdateTokenRequest): Response<UpdateTokenResponse> {
        val iRestService = ApiUtils.getCurrentRestService()
        val method = iRestService::updateFirebaseToken
        return method.invoke(request)
    }

    suspend fun getAllCategories() = dao.getAllCategories()

    suspend fun getPlace(id: String) = dao.getPlace(id)

    fun getPlaces() = dao.getPlaces()

    suspend fun insertPlace(vararg place : Place) = dao.insertPlace(*place)

    suspend fun getCategories(): Response<CategoryResponse> {
        val iRestService = ApiUtils.getCurrentRestService()
        val method = iRestService::getCategories
        return method.invoke()
    }

    suspend fun getPlaces(url: String): Response<GooglePlaceResponse> {
        val iRestService = ApiUtils.getCurrentRestService()
        val method = iRestService::getPlaces
        return method.invoke(url)
    }

    companion object {

        @Volatile
        private var instance: ResourceRepository? = null

        fun getInstance(dao: ResourcesDao) =
                instance ?: synchronized(this) {
                    instance ?: ResourceRepository(dao).also { instance = it }
                }
    }
}
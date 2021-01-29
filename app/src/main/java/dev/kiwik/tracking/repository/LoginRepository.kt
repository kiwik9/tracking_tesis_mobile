package dev.kiwik.tracking.repository

import dev.kiwik.tracking.domain.ApiUtils
import dev.kiwik.tracking.domain.ResultWrapper
import dev.kiwik.tracking.domain.api.LoginGoogleRequest
import dev.kiwik.tracking.domain.api.LoginRequest
import dev.kiwik.tracking.domain.api.LoginResponse
import dev.kiwik.tracking.domain.entities.UserRequest
import dev.kiwik.tracking.domain.safeApiCall

class LoginRepository {

    suspend fun login(email: String, password: String): ResultWrapper<LoginResponse> {
        val iRestService = ApiUtils.getCurrentRestService()
        return safeApiCall {
            iRestService.login(LoginRequest(email, password))
        }
    }

    suspend fun loginWithGoogle(email: String, phone: String, name: String): ResultWrapper<LoginResponse> {
        val iRestService = ApiUtils.getCurrentRestService()
        return safeApiCall {
            iRestService.loginWithGoogle(LoginGoogleRequest(email, phone, name))
        }
    }

    suspend fun register(user: UserRequest): ResultWrapper<LoginResponse> {
        val iRestService = ApiUtils.getCurrentRestService()
        return safeApiCall {
            iRestService.register(user)
        }
    }

    suspend fun updateUser(user: UserRequest): ResultWrapper<LoginResponse> {
        val iRestService = ApiUtils.getCurrentRestService()
        return safeApiCall {
            iRestService.updateUser(user)
        }
    }

}
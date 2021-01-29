package dev.kiwik.tracking.viewmodel

import androidx.lifecycle.ViewModel
import dev.kiwik.tracking.domain.entities.UserRequest
import dev.kiwik.tracking.repository.LoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        repository.login(email, password)
    }

    suspend fun loginWithGoogle(email: String, phone: String, name: String) = withContext(Dispatchers.IO) {
        repository.loginWithGoogle(email, phone, name)
    }

    suspend fun register(user: UserRequest) = withContext(Dispatchers.IO) {
        repository.register(user)
    }

    suspend fun updateUser(user: UserRequest) = withContext(Dispatchers.IO) {
        repository.updateUser(user)
    }

}
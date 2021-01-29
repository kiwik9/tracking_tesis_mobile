package dev.kiwik.tracking.domain.api

import com.squareup.moshi.Moshi
import dev.kiwik.tracking.preferences.Pref
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException

object ApiUtils {
    private var iRestService: IRestService? = null

    fun changeBaseUrl(BASE_URL: String) {
        RetrofitClient.changeBaseUrl(BASE_URL)
    }

    fun getCurrentRestService(): IRestService {
        return iRestService ?: buildIRestService().also { iRestService = it }
    }

    private fun buildIRestService(): IRestService {
        val token = Pref.getInstance().values.loggedUser?.token

        if (token != null) {
            return RetrofitClient.createAuthToken(IRestService::class.java, "Bearer", token)
        }
        return RetrofitClient.create(IRestService::class.java)
    }

    fun updateToken() {
        iRestService = buildIRestService()
    }
}

suspend fun <T, R> Iterable<T>.mapAsync(transform: suspend (T) -> R): List<R> {
    return coroutineScope {
        val list = this@mapAsync.map { e ->
            async {
                transform(e)
            }
        }
        list.map { it.await() }
    }
}

suspend fun <T> Iterable<T>.forEachAsync(transform: suspend (T) -> Unit) {
    return coroutineScope {
        val list = this@forEachAsync.map { e ->
            async {
                transform(e)
            }
        }
        list.forEach { it.await() }
    }
}

sealed class ResultWrapper<out T> {

    data class Success<out T>(val value: T): ResultWrapper<T>()
    data class GenericError(val response: ApiError): ResultWrapper<Nothing>()
    object NetworkError: ResultWrapper<Nothing>()

    val isSuccess = this is Success

    fun getOrNull(): T? = when(this) {
        is Success -> value
        else -> null
    }

    fun getOrError(onError: (ResultWrapper<Nothing>) -> Unit): T? = when(this) {
        is Success -> value
        else -> {
            onError(this as ResultWrapper<Nothing>)
            null
        }
    }

    companion object {
        fun handleError(exception: Throwable) = when(exception) {
            is IOException -> NetworkError
            is HttpException -> {
                val errorResponse = convertErrorBody(exception)
                GenericError(errorResponse)
            } else -> {
                val customError = ApiError(exception.message ?: "", 500)
                GenericError(customError)
            }
        }

        /**
         * Serializa el error de una llamada a la API
         */
        fun convertErrorBody(throwable: HttpException): ApiError {
            return try {
                throwable.response()?.errorBody()?.source()?.let {
                    val adapter = Moshi.Builder().build().adapter(ApiError::class.java)
                    adapter.fromJson(it)
                } ?: ApiError("Error desconocido", 500)
            } catch (exception: Exception) {
                ApiError("Error desconocido", 500)
            }
        }
    }
}

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher = Dispatchers.IO, call: suspend () -> T): ResultWrapper<T> {
    return try {
        withContext(dispatcher) {
            ResultWrapper.Success(call.invoke())
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        ResultWrapper.handleError(e)
    }
}

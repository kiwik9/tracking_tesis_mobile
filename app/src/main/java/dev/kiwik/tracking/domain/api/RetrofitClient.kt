package dev.kiwik.tracking.domain.api

import android.text.TextUtils
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import dev.kiwik.tracking.BuildConfig
import dev.kiwik.tracking.MvpApp
import dev.kiwik.tracking.ui.activity.LoginActivity
import dev.kiwik.tracking.utilities.CustomDateTimeAdapter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    var TAG: String = RetrofitClient::class.java.simpleName
    private var BASE_URL = BuildConfig.BASE_URL

    private lateinit var retrofit: Retrofit
    private val moshi = Moshi.Builder().add(CustomDateTimeAdapter()).build()
    private val logging = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val okHttpClient = enableTlsOnPreLollipop(
        OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addNetworkInterceptor(StethoInterceptor())
    )

    private var builder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))

    fun changeBaseUrl(newApiBaseUrl: String) {
        BASE_URL = newApiBaseUrl
        builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
    }

    fun <S> create(serviceClass: Class<S>): S {
        return create(serviceClass, "", "")

    }

    fun <S> createAuthToken(serviceClass: Class<S>, typeAuth: String, tokenAuth: String): S {
        return create(serviceClass, typeAuth, tokenAuth)
    }

    private fun <S> create(serviceClass: Class<S>, typeAuth: String, tokenAuth: String): S {
        okHttpClient!!.interceptors().clear() // Limpia los interceptores por si antes tenía auth token y ahora no.
        okHttpClient.addInterceptor(logging)

        if (!TextUtils.isEmpty(tokenAuth)) {
            val authInterceptor = AuthenticationInterceptor(typeAuth, tokenAuth)
            if (!okHttpClient.interceptors().contains(authInterceptor)) {
                okHttpClient.addInterceptor(authInterceptor)
            }
        }
        builder.client(okHttpClient.build())

        retrofit = builder.build()
        return retrofit.create(serviceClass)
    }

    /*
     *
     *   Reference: https://futurestud.io/tutorials/android-basic-authentication-with-retrofit
     * */
    private class AuthenticationInterceptor(
        private val typeAuth: String,
        private val authToken: String
    ) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()

            val builder = original.newBuilder()
            builder.header("Accept", "application/json") //if necessary, say to consume JSON);
            setAuthHeader(builder, authToken, typeAuth)

            val request = builder.build()
            val response = chain.proceed(request)

            if (response.code() == 401) { //if unauthorized

                okHttpClient?.let {
                    synchronized(it) {
                        LoginActivity.startActivity(MvpApp.instance.applicationContext)
                    }
                }
            }
            return response
        }
    }

    private fun setAuthHeader(builder: Request.Builder, authToken: String?, typeAuth: String) {
        if (authToken != null)
        //Add Auth token to each request if authorized
            builder.header("Authorization", "$typeAuth $authToken")
    }

    private fun enableTlsOnPreLollipop(client: OkHttpClient.Builder): OkHttpClient.Builder? {
        return client
    }
}
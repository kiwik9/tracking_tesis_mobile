package dev.kiwik.tracking.domain.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: Int,
    val name: String,
    val lastname: String,
    val email: String,
    val address: String,
    val phone: String,
    val token: String,
    val preferences : String
)

data class UserRequest(
        val name: String,
        val lastname: String,
        val password : String,
        val email: String,
        val address: String,
        val phone: String,
        val token: String = "Non Token",
        val preferences : String = "Non preferences",
        val userId : Int = -1
)

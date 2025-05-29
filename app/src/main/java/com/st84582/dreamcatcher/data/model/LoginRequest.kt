// src/main/java/com/st84582/dreamcatcher/data/model/LoginRequest.kt
package com.st84582.dreamcatcher.data.model

import com.google.gson.annotations.SerializedName
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
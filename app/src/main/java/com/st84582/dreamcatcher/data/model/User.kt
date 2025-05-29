package com.st84582.dreamcatcher.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Long,

    @SerializedName("email")
    val email: String
)
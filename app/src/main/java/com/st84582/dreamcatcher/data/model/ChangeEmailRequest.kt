package com.st84582.dreamcatcher.data.model

import com.google.gson.annotations.SerializedName

data class ChangeEmailRequest(
    @SerializedName("newEmail")
    val newEmail: String,
    @SerializedName("currentPassword")
    val currentPassword: String
)
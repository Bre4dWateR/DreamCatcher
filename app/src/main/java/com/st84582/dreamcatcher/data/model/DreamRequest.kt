package com.st84582.dreamcatcher.data.model

data class DreamRequest(
    val id: Long? = null,
    val title: String,
    val story: String,
    val date: String,
    val userId: Long
)
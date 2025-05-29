package com.st84582.dreamcatcher.data.api

import com.st84582.dreamcatcher.data.model.ChangeEmailRequest
import com.st84582.dreamcatcher.data.model.Dream
import com.st84582.dreamcatcher.data.model.DreamRequest
import com.st84582.dreamcatcher.data.model.LoginRequest
import com.st84582.dreamcatcher.data.model.RegistrationRequest
import com.st84582.dreamcatcher.data.model.User
import com.st84582.dreamcatcher.data.model.ChangePasswordRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/users/register")
    fun registerUser(@Body registrationRequest: RegistrationRequest): Call<User>

    @POST("/api/users/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<User>

    @GET("/api/dreams/user/{userId}")
    fun getDreamsByUserId(@Path("userId") userId: Long): Call<List<Dream>>

    @GET("/api/dreams/{dreamId}/user/{userId}")
    fun getDreamByIdAndUserId(
        @Path("dreamId") dreamId: Long,
        @Path("userId") userId: Long
    ): Call<Dream>

    @POST("/api/dreams")
    fun createDream(@Body dreamRequest: DreamRequest): Call<Dream>

    @PUT("/api/dreams/{dreamId}")
    fun updateDream(
        @Path("dreamId") dreamId: Long,
        @Body dreamRequest: DreamRequest
    ): Call<Dream>

    @DELETE("/api/dreams/{dreamId}/user/{userId}")
    fun deleteDream(
        @Path("dreamId") dreamId: Long,
        @Path("userId") userId: Long
    ): Call<Void>

    @PUT("/api/users/{id}/change-email")
    fun changeEmail(
        @Path("id") userId: Long,
        @Body request: ChangeEmailRequest
    ): Call<User>

    @PUT("/api/users/{id}/change-password")
    fun changePassword(
        @Path("id") userId: Long,
        @Body request: ChangePasswordRequest
    ): Call<User>

    @DELETE("/api/users/{userId}")
    fun deleteUser(@Path("userId") userId: Long): Call<Void>
}

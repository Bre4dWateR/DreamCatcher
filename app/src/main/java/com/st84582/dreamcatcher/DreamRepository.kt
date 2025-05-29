package com.st84582.dreamcatcher.data.repository

import com.st84582.dreamcatcher.data.api.RetrofitClient
import com.st84582.dreamcatcher.data.model.Dream
import com.st84582.dreamcatcher.data.model.DreamRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DreamRepository {

    private val apiService = RetrofitClient.apiService

    fun getDreamsByUserId(userId: Long, callback: (Result<List<Dream>>) -> Unit) {
        apiService.getDreamsByUserId(userId).enqueue(object : Callback<List<Dream>> {
            override fun onResponse(call: Call<List<Dream>>, response: Response<List<Dream>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(Result.success(it))
                    } ?: callback(Result.failure(Exception("Response body is null")))
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(Result.failure(Exception("Error fetching dreams: ${response.code()} - $errorBody")))
                }
            }

            override fun onFailure(call: Call<List<Dream>>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    fun createDream(dreamRequest: DreamRequest, callback: (Result<Dream>) -> Unit) {
        apiService.createDream(dreamRequest).enqueue(object : Callback<Dream> {
            override fun onResponse(call: Call<Dream>, response: Response<Dream>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(Result.success(it))
                    } ?: callback(Result.failure(Exception("Response body is null")))
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(Result.failure(Exception("Error creating dream: ${response.code()} - $errorBody")))
                }
            }

            override fun onFailure(call: Call<Dream>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }


    fun updateDream(dreamId: Long, dreamRequest: DreamRequest, callback: (Result<Dream>) -> Unit) {
        apiService.updateDream(dreamId, dreamRequest).enqueue(object : Callback<Dream> {
            override fun onResponse(call: Call<Dream>, response: Response<Dream>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(Result.success(it))
                    } ?: callback(Result.failure(Exception("Response body is null")))
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(Result.failure(Exception("Error updating dream: ${response.code()} - $errorBody")))
                }
            }

            override fun onFailure(call: Call<Dream>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    fun getDreamByIdAndUserId(dreamId: Long, userId: Long, callback: (Result<Dream>) -> Unit) {
        apiService.getDreamByIdAndUserId(dreamId, userId).enqueue(object : Callback<Dream> {
            override fun onResponse(call: Call<Dream>, response: Response<Dream>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(Result.success(it))
                    } ?: callback(Result.failure(Exception("Response body is null")))
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(Result.failure(Exception("Error fetching dream by ID: ${response.code()} - $errorBody")))
                }
            }

            override fun onFailure(call: Call<Dream>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    fun deleteDream(dreamId: Long, userId: Long, callback: (Result<Unit>) -> Unit) {
        apiService.deleteDream(dreamId, userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    callback(Result.success(Unit))
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(Result.failure(Exception("Error deleting dream: ${response.code()} - $errorBody")))
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }
}

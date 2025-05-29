package com.st84582.dreamcatcher

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.st84582.dreamcatcher.data.api.ApiClient
import com.st84582.dreamcatcher.data.model.ChangeEmailRequest
import com.st84582.dreamcatcher.data.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangeEmailActivity : AppCompatActivity() {

    private lateinit var backIconChangeEmail: ImageView
    private lateinit var newEmailEditText: EditText
    private lateinit var currentPasswordEditText: EditText
    private lateinit var changeEmailButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)
        backIconChangeEmail = findViewById(R.id.backIconChangeEmail)
        newEmailEditText = findViewById(R.id.editTextNewEmail)
        currentPasswordEditText = findViewById(R.id.editTextCurrentPasswordEmail)
        changeEmailButton = findViewById(R.id.buttonSaveNewEmail)

        backIconChangeEmail.setOnClickListener {
            finish()
        }

        changeEmailButton.setOnClickListener {
            val newEmail = newEmailEditText.text.toString().trim()
            val password = currentPasswordEditText.text.toString().trim()

            if (newEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please, fill in the fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getLong("user_id", -1L)

            if (userId == -1L) {
                Toast.makeText(this, "User unauthorised!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apiService = ApiClient.apiService
            val request = ChangeEmailRequest(newEmail, password)

            apiService.changeEmail(userId, request).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChangeEmailActivity, "Email successfully saved!", Toast.LENGTH_SHORT).show()
                        sharedPref.edit().putString("user_email", newEmail).apply()
                        finish()
                    } else {
                        val errorMessage = when (response.code()) {
                            400 -> "Invalid request. Check your entered data!"
                            401 -> "Current password is incorrect!"
                            404 -> "User not found!"
                            409 -> "Email is already taken by another user!"
                            else -> "Failed to change Email: ${response.code()}"
                        }
                        Toast.makeText(this@ChangeEmailActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@ChangeEmailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    t.printStackTrace()
                }
            })
        }
    }
}
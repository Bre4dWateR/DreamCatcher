package com.st84582.dreamcatcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.st84582.dreamcatcher.data.api.ApiClient
import com.st84582.dreamcatcher.data.model.ChangePasswordRequest
import com.st84582.dreamcatcher.data.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var backIconChangePassword: ImageView
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var saveNewPasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        backIconChangePassword = findViewById(R.id.backIconChangePassword)
        currentPasswordEditText = findViewById(R.id.editTextCurrentPassword)
        newPasswordEditText = findViewById(R.id.editTextNewPassword)
        confirmNewPasswordEditText = findViewById(R.id.editTextConfirmNewPassword)
        saveNewPasswordButton = findViewById(R.id.buttonSaveNewPassword)

        backIconChangePassword.setOnClickListener {
            finish()
        }

        saveNewPasswordButton.setOnClickListener {
            val oldPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmNewPassword = confirmNewPasswordEditText.text.toString().trim()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (oldPassword == newPassword) {
                Toast.makeText(this, "New password cannot be the same as the old password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmNewPassword) {
                Toast.makeText(this, "New passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getLong("user_id", -1L)

            if (userId == -1L) {
                Toast.makeText(this, "User unauthorized!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return@setOnClickListener
            }

            val apiService = ApiClient.apiService
            val request = ChangePasswordRequest(oldPassword, newPassword)

            apiService.changePassword(userId, request).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChangePasswordActivity, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMessage = when (response.code()) {
                            400 -> "Invalid request, please check data!"
                            401 -> "Incorrect current password!"
                            404 -> "User not found!"
                            409 -> "New password cannot be the same as the old password!"
                            else -> "Password change failed: ${response.code()}"
                        }
                        Toast.makeText(this@ChangePasswordActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@ChangePasswordActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    t.printStackTrace()
                }
            })
        }
    }
}

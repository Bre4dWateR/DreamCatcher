package com.st84582.dreamcatcher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.st84582.dreamcatcher.data.api.ApiService
import com.st84582.dreamcatcher.data.api.ApiClient
import com.st84582.dreamcatcher.data.model.RegistrationRequest
import com.st84582.dreamcatcher.data.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import org.json.JSONObject
import android.widget.ImageView

class RegistrationActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var submitRegistrationButton: Button
    private lateinit var backIconRegistration: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        apiService = ApiClient.apiService

        emailEditText = findViewById(R.id.editTextRegEmail)
        passwordEditText = findViewById(R.id.editTextRegPassword)
        confirmPasswordEditText = findViewById(R.id.editTextRegConfirmPassword)
        submitRegistrationButton = findViewById(R.id.buttonSubmitRegistration)
        backIconRegistration = findViewById(R.id.backIconRegistration)

        submitRegistrationButton.setOnClickListener {
            performRegistration()
        }

        backIconRegistration.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val registrationRequest = RegistrationRequest(email = email, password = password)

        apiService.registerUser(registrationRequest).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val registeredUser = response.body()
                    Toast.makeText(this@RegistrationActivity, "Registration successful! Welcome, ${registeredUser?.email}", Toast.LENGTH_SHORT).show()
                    Log.d("Registration", "Successful registration: ${registeredUser?.email}")

                    val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    Log.e("Registration", "Registration error: ${response.code()} - $errorBodyString")
                    val errorMessage = try {
                        val json = JSONObject(errorBodyString)
                        json.optString("message", "Unknown registration error")
                    } catch (e: Exception) {
                        response.message()
                    }
                    Toast.makeText(this@RegistrationActivity, "Registration error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("Registration", "Network failure during registration: ${t.message}", t)
                Toast.makeText(this@RegistrationActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}


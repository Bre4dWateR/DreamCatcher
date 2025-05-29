package com.st84582.dreamcatcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.st84582.dreamcatcher.data.api.ApiService
import com.st84582.dreamcatcher.data.api.ApiClient
import com.st84582.dreamcatcher.data.model.LoginRequest
import com.st84582.dreamcatcher.data.model.User

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiService = ApiClient.apiService

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.buttonLogin)
        registerButton = findViewById(R.id.buttonRegister)

        loginButton.setOnClickListener {
            performLogin()
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        emailEditText.text.clear()
        passwordEditText.text.clear()
    }

    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and password!", Toast.LENGTH_SHORT).show()
            return
        }

        val loginRequest = LoginRequest(email = email, password = password)
        apiService.loginUser(loginRequest).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val loggedInUser = response.body()
                    Toast.makeText(this@MainActivity, "Welcome, ${loggedInUser?.email}", Toast.LENGTH_SHORT).show()
                    Log.d("Login", "Successful login: ${loggedInUser?.email}")

                    val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPrefs.edit()
                    loggedInUser?.id?.let { editor.putLong("user_id", it) } ?: editor.remove("user_id")
                    editor.putString("user_email", loggedInUser?.email)
                    editor.remove("user_name") // Remove user_name as we are not using nickname/username
                    editor.apply()

                    val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                    intent.putExtra("user_email", loggedInUser?.email)
                    intent.putExtra("user_id", loggedInUser?.id)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    Log.e("Login", "Login error: ${response.code()} - $errorBodyString")
                    val errorMessage = try {
                        val json = org.json.JSONObject(errorBodyString)
                        json.optString("message", "Unknown login error!")
                    } catch (e: Exception) {
                        response.message()
                    }
                    Toast.makeText(this@MainActivity, "Login error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("Login", "Network error: ${t.message}", t)
                Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

package com.st84582.dreamcatcher

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback

import com.st84582.dreamcatcher.data.api.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var buttonUserEmail: Button
    private lateinit var buttonChangeEmail: Button
    private lateinit var buttonChangePassword: Button
    private lateinit var buttonDeleteAccount: Button
    private lateinit var backIconProfile: ImageView

    private var currentUserId: Long = -1L

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        currentUserId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getLong("user_id", -1L)

        if (currentUserId == -1L) {
            Toast.makeText(this, "Error: User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        buttonUserEmail = findViewById(R.id.buttonUserEmail)
        buttonChangeEmail = findViewById(R.id.buttonChangeEmail)
        buttonChangePassword = findViewById(R.id.buttonChangePassword)
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount)
        backIconProfile = findViewById(R.id.backIconProfile)

        backIconProfile.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        buttonChangeEmail.setOnClickListener {
            val intent = Intent(this, ChangeEmailActivity::class.java)
            startActivity(intent)
        }

        buttonChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        buttonDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        displayUserEmail()
    }

    private fun displayUserEmail() {
        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPrefs.getString("user_email", "Unknown email")
        buttonUserEmail.text = userEmail
    }

    private fun showDeleteAccountConfirmationDialog() {
        // ИЗМЕНЕНИЕ ЗДЕСЬ: Явно указываем тему для AlertDialog
        AlertDialog.Builder(this, R.style.AlertDialogTheme) //
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? We will miss you =(")
            .setPositiveButton("Delete") { dialog, which ->
                deleteUserAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUserAccount() {
        if (currentUserId == -1L) {
            Toast.makeText(this, "Failed to delete account: User ID not found.", Toast.LENGTH_LONG).show()
            return
        }

        ApiClient.apiService.deleteUser(currentUserId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Account deleted successfully.", Toast.LENGTH_LONG).show()
                    clearUserDataAndRedirectToLogin()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@ProfileActivity, "Account deletion error: ${response.code()} - ${errorBody ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network error during account deletion: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun clearUserDataAndRedirectToLogin() {
        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        displayUserEmail()
    }
}

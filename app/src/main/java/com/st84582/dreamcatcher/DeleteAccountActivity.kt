package com.st84582.dreamcatcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.st84582.dreamcatcher.data.api.ApiService
import com.st84582.dreamcatcher.data.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var deleteAccountButton: Button
    private lateinit var backIcon: ImageView
    private var currentUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        apiService = RetrofitClient.apiService

        deleteAccountButton = findViewById(R.id.buttonDeleteMyAccount)
        backIcon = findViewById(R.id.backIconDeleteAccount)

        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUserId = sharedPrefs.getLong("user_id", -1L)

        Log.d("DeleteAccountActivity", "Retrieved userId for deletion: $currentUserId")

        if (currentUserId == -1L) {
            Toast.makeText(this, "User unauthorised, please try again!", Toast.LENGTH_LONG).show()
            Log.e("DeleteAccountActivity", "User ID is -1, cannot proceed with deletion.")
            finish()
            return
        }

        backIcon.setOnClickListener {
            finish()
        }

        deleteAccountButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete account?")
            .setMessage("Are you sure you want to delete account?")
            .setPositiveButton("Delete") { dialog, which ->
                performAccountDeletion(currentUserId)
            }
            .setNegativeButton("Back", null)
            .show()
    }

    private fun performAccountDeletion(userId: Long) {
        if (userId == -1L) {
            Toast.makeText(this, "Error. User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.deleteUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DeleteAccountActivity, "Account deleted!", Toast.LENGTH_LONG).show()
                    Log.d("DeleteAccountActivity", "Account deleted successfully for userId: $userId")

                    val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().clear().apply()
                    Log.d("DeleteAccountActivity", "SharedPreferences cleared after account deletion.")

                    val intent = Intent(this@DeleteAccountActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val json = org.json.JSONObject(errorBody)
                        json.optString("message", "Unknown error in account delete!")
                    } catch (e: Exception) {
                        "Account delete error: ${response.code()} - ${response.message()}"
                    }
                    Toast.makeText(this@DeleteAccountActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("DeleteAccountActivity", "Error deleting account: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@DeleteAccountActivity, "Network error in account delete: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("DeleteAccountActivity", "Network error deleting account: ${t.message}", t)
            }
        })
    }
}

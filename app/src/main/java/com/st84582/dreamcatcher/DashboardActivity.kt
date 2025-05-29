package com.st84582.dreamcatcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.st84582.dreamcatcher.data.api.ApiService
import com.st84582.dreamcatcher.data.api.ApiClient
import com.st84582.dreamcatcher.data.model.Dream
import com.st84582.dreamcatcher.data.model.User

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private var currentUserId: Long = -1L
    private lateinit var usernameTextView: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var dreamAdapter: DreamAdapter
    private lateinit var fabAddDream: FloatingActionButton
    private lateinit var bottomNavigationView: BottomNavigationView

    private val ADD_EDIT_DREAM_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        apiService = ApiClient.apiService

        usernameTextView = findViewById(R.id.usernameTextView)

        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUserId = sharedPrefs.getLong("user_id", -1L)
        val username = sharedPrefs.getString("user_name", "Гость")

        Log.d("DashboardActivity", "Retrieved userId from SharedPreferences: $currentUserId")
        Log.d("DashboardActivity", "Retrieved username from SharedPreferences: $username")


        if (currentUserId == -1L) {
            Toast.makeText(this, "User unauthorised, please try again!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        usernameTextView.text = "Hi, Dreamer!"

        recyclerView = findViewById(R.id.recyclerViewDreams)
        recyclerView.layoutManager = LinearLayoutManager(this)
        dreamAdapter = DreamAdapter(mutableListOf()) { dream ->
            val intent = Intent(this, DreamDetailsActivity::class.java)
            intent.putExtra("DREAM_ID", dream.id)
            startActivity(intent)
        }
        recyclerView.adapter = dreamAdapter

        fabAddDream = findViewById(R.id.fabAddDream)
        fabAddDream.setOnClickListener {
            val intent = Intent(this, AddEditDreamActivity::class.java)
            startActivityForResult(intent, ADD_EDIT_DREAM_REQUEST_CODE)
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    Toast.makeText(this, "Home page", Toast.LENGTH_SHORT).show()
                    loadUserDreams(currentUserId)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        loadUserDreams(currentUserId)
    }

    override fun onResume() {
        super.onResume()
        loadUserDreams(currentUserId)
        bottomNavigationView.selectedItemId = R.id.navigation_home
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EDIT_DREAM_REQUEST_CODE && resultCode == RESULT_OK) {
            loadUserDreams(currentUserId)
        }
    }

    private fun loadUserDreams(userId: Long) {
        Log.d("DashboardActivity", "Loading dreams for user ID: $userId")
        apiService.getDreamsByUserId(userId).enqueue(object : Callback<List<Dream>> {
            override fun onResponse(call: Call<List<Dream>>, response: Response<List<Dream>>) {
                if (response.isSuccessful) {
                    val dreams = response.body()
                    dreams?.let {
                        if (it.isEmpty()) {
                            Toast.makeText(this@DashboardActivity, "Dreams not found. Add new dream!", Toast.LENGTH_LONG).show()
                            Log.d("DashboardActivity", "No dreams found for user.")
                            dreamAdapter.updateDreams(emptyList())
                        } else {
                            dreamAdapter.updateDreams(it)
                            Toast.makeText(this@DashboardActivity, "Dreams loaded: ${it.size}", Toast.LENGTH_SHORT).show()
                            Log.d("DashboardActivity", "Dreams loaded: ${it.size}")
                        }
                    } ?: run {
                        dreamAdapter.updateDreams(emptyList())
                        Toast.makeText(this@DashboardActivity, "Unable to find the dreams!", Toast.LENGTH_SHORT).show()
                        Log.e("DashboardActivity", "Dream list is null in response.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@DashboardActivity, "Error in dream loading: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    Log.e("DashboardActivity", "Error loading dreams: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<List<Dream>>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Network error in dream downloading: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("DashboardActivity", "Network error loading dreams: ${t.message}", t)
            }
        })
    }
}
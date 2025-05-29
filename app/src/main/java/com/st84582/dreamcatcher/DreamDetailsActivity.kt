package com.st84582.dreamcatcher

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView // Добавлено для backIconDetails
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.st84582.dreamcatcher.data.api.ApiClient
import com.st84582.dreamcatcher.data.api.ApiService
import com.st84582.dreamcatcher.data.model.Dream

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DreamDetailsActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private var currentUserId: Long = -1L
    private var currentDreamId: Long = -1L

    private lateinit var backIconDetails: ImageView
    private lateinit var dreamTitleTextView: TextView
    private lateinit var dreamStoryTextView: TextView
    private lateinit var dreamDateTextView: TextView
    private lateinit var editDreamButton: Button
    private lateinit var deleteDreamButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dream_details)

        apiService = ApiClient.apiService

        backIconDetails = findViewById(R.id.backIconDetails)
        dreamTitleTextView = findViewById(R.id.textViewDreamTitle)
        dreamStoryTextView = findViewById(R.id.textViewDreamStory)
        dreamDateTextView = findViewById(R.id.textViewDreamDate)
        editDreamButton = findViewById(R.id.buttonEditDream)
        deleteDreamButton = findViewById(R.id.buttonDeleteDream)

        backIconDetails.setOnClickListener {
            finish()
        }

        currentDreamId = intent.getLongExtra("DREAM_ID", -1L)
        currentUserId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getLong("user_id", -1L)

        Log.d("DreamDetailsActivity", "Retrieved userId from SharedPreferences: $currentUserId")

        if (currentDreamId != -1L && currentUserId != -1L) {
            loadDreamDetails(currentDreamId, currentUserId)
        } else {
            Toast.makeText(this, "Unable to load a dream. Incorrect ID!", Toast.LENGTH_SHORT).show()
            Log.e("DreamDetailsActivity", "Invalid DREAM_ID or USER_ID received: dreamId=$currentDreamId, userId=$currentUserId")
            finish()
        }

        editDreamButton.setOnClickListener {
            val intent = Intent(this, AddEditDreamActivity::class.java)
            intent.putExtra("DREAM_ID", currentDreamId)
            startActivity(intent)
        }

        deleteDreamButton.setOnClickListener {
            confirmAndDeleteDream(currentDreamId)
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentDreamId != -1L && currentUserId != -1L) {
            loadDreamDetails(currentDreamId, currentUserId)
        }
    }

    private fun loadDreamDetails(dreamId: Long, userId: Long) {
        Log.d("DreamDetailsActivity", "Loading dream with ID: $dreamId for user: $userId")
        apiService.getDreamByIdAndUserId(dreamId, userId).enqueue(object : Callback<Dream> {
            override fun onResponse(call: Call<Dream>, response: Response<Dream>) {
                if (response.isSuccessful) {
                    val dream = response.body()
                    if (dream != null) {
                        Log.d("DreamDetailsActivity", "Dream loaded: ${dream.title}, Date: ${dream.date}, Story: ${dream.story}")
                        dreamTitleTextView.text = dream.title // Изменено на dreamTitleTextView
                        dreamStoryTextView.text = dream.story // Изменено на dreamStoryTextView
                        formatAndSetDate(dream.date, dreamDateTextView) // Изменено на dreamDateTextView
                    } else {
                        Toast.makeText(this@DreamDetailsActivity, "Dream not found!", Toast.LENGTH_SHORT).show()
                        Log.e("DreamDetailsActivity", "Dream object is null in response.")
                        finish()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@DreamDetailsActivity, "Dream loading error: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    Log.e("DreamDetailsActivity", "Error loading dream: ${response.code()} - $errorBody")
                    finish()
                }
            }

            override fun onFailure(call: Call<Dream>, t: Throwable) {
                Toast.makeText(this@DreamDetailsActivity, "Error loading dream: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("DreamDetailsActivity", "Network error loading dream: ${t.message}", t)
                finish()
            }
        })
    }

    private fun confirmAndDeleteDream(dreamId: Long) {
        Toast.makeText(this, "Hold button to delete a dream!", Toast.LENGTH_LONG).show()

        deleteDreamButton.setOnLongClickListener {
            deleteDream(dreamId)
            true
        }
    }

    private fun deleteDream(dreamId: Long) {
        apiService.deleteDream(dreamId, currentUserId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DreamDetailsActivity, "Dream successfully deleted!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@DreamDetailsActivity, "Error deleting dream: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    Log.e("DreamDetailsActivity", "Error deleting dream: ${response.code()} - $errorBody")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@DreamDetailsActivity, "Network error in dream delete: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("DreamDetailsActivity", "Network error deleting dream: ${t.message}", t)
            }
        })
    }

    private fun formatAndSetDate(dateString: String, textView: TextView) {
        if (dateString.isEmpty()) {
            textView.text = "No date!"
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val parsedDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                val desiredFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = parsedDate.format(desiredFormatter)
                textView.text = formattedDate
            } catch (e: Exception) {
                Log.e("DreamDetailsActivity", "Error parsing date with LocalDate: ${e.message}", e)
                textView.text = dateString
            }
        } else {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                if (date != null) {
                    textView.text = outputFormat.format(date)
                } else {
                    textView.text = dateString
                }
            } catch (e: Exception) {
                Log.e("DreamDetailsActivity", "Error parsing date with SimpleDateFormat: ${e.message}", e)
                textView.text = dateString
            }
        }
    }
}


package com.st84582.dreamcatcher

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

import com.st84582.dreamcatcher.data.api.ApiClient
import com.st84582.dreamcatcher.data.model.Dream
import com.st84582.dreamcatcher.data.model.DreamRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale



class AddEditDreamActivity : AppCompatActivity() {

    private lateinit var backIconAddEdit: ImageView
    private lateinit var editTextDreamTitle: EditText
    private lateinit var editTextDreamStory: EditText
    private lateinit var buttonUploadDream: Button
    private lateinit var editTextDreamDate: EditText
    private var currentDreamId: Long = -1L
    private var currentUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_dream)

        backIconAddEdit = findViewById(R.id.backIconAddEdit)
        editTextDreamTitle = findViewById(R.id.editTextDreamTitle)
        editTextDreamStory = findViewById(R.id.editTextDreamStory)
        buttonUploadDream = findViewById(R.id.buttonUploadDream)
        editTextDreamDate = findViewById(R.id.editTextDreamDate)


        backIconAddEdit.setOnClickListener {
            finish()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        currentDreamId = intent.getLongExtra("DREAM_ID", -1L)
        currentUserId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getLong("user_id", -1L)

        Log.d("AddEditDreamActivity", "Retrieved userId: $currentUserId")

        if (currentUserId == -1L) {
            Toast.makeText(this, "User unauthorised. Please, try to enter again!", Toast.LENGTH_SHORT).show()
            Log.e("AddEditDreamActivity", "User ID is -1, finishing activity.")
            finish()
            return
        }

        if (currentDreamId != -1L) {
            title = "Edit dream"
            buttonUploadDream.text = "Update dream"
            loadDreamForEdit(currentDreamId, currentUserId)
        } else {
            title = "Add new"
            buttonUploadDream.text = "Add dream"
            val todayDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } else {
                java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date())
            }
            editTextDreamDate.setText(todayDate)
        }

        buttonUploadDream.setOnClickListener {
            saveDream()
        }
    }

    private fun loadDreamForEdit(dreamId: Long, userId: Long) {
        ApiClient.apiService.getDreamByIdAndUserId(dreamId, userId).enqueue(object : Callback<Dream> {
            override fun onResponse(call: Call<Dream>, response: Response<Dream>) {
                if (response.isSuccessful) {
                    val dream = response.body()
                    if (dream != null) {
                        editTextDreamTitle.setText(dream.title)
                        editTextDreamStory.setText(dream.story)

                        val originalDateString = dream.date
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            try {
                                val parsedDate = LocalDate.parse(originalDateString, DateTimeFormatter.ISO_LOCAL_DATE)
                                val desiredFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                val formattedDate = parsedDate.format(desiredFormatter)
                                editTextDreamDate.setText(formattedDate)
                            } catch (e: Exception) {
                                Log.e("AddEditDream", "Error parsing date with LocalDate: ${e.message}")
                                editTextDreamDate.setText(originalDateString) // Fallback
                            }
                        } else {
                            try {
                                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val date = inputFormat.parse(originalDateString)
                                if (date != null) {
                                    val formattedDate = outputFormat.format(date)
                                    editTextDreamDate.setText(formattedDate)
                                } else {
                                    editTextDreamDate.setText(originalDateString) // Fallback
                                }
                            } catch (e: Exception) {
                                Log.e("AddEditDream", "Error parsing date with SimpleDateFormat: ${e.message}")
                                editTextDreamDate.setText(originalDateString) // Fallback
                            }
                        }
                    } else {
                        Toast.makeText(this@AddEditDreamActivity, "Drea, is not found!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@AddEditDreamActivity, "Error in downloading or editing dream: ${response.code()} - $errorBody", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Dream>, t: Throwable) {
                Toast.makeText(this@AddEditDreamActivity, "Network error in downloading or editing dream: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("AddEditDream", "Network error loading dream for edit: ${t.message}", t)
                finish()
            }
        })
    }

    private fun saveDream() {
        val title = editTextDreamTitle.text.toString().trim()
        val story = editTextDreamStory.text.toString().trim()
        val displayedDateString = editTextDreamDate.text.toString().trim()

        if (title.isEmpty() || story.isEmpty() || displayedDateString.isEmpty()) {
            Toast.makeText(this, "Please, fill in the fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val dateToSend: String = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val parsed = LocalDate.parse(displayedDateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                parsed.format(DateTimeFormatter.ISO_LOCAL_DATE)
            } else {
                val inputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(displayedDateString)
                if (date != null) outputFormat.format(date) else {
                    Log.e("AddEditDream", "Fallback to current date for parsing error (pre-Oreo)")
                    java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
                }
            }
        } catch (e: Exception) {
            Log.e("AddEditDream", "Error parsing displayed date string '$displayedDateString': ${e.message}")
            Toast.makeText(this, "Incorrect date format!", Toast.LENGTH_LONG).show()
            // Возвращаем текущую дату как запасной вариант, чтобы избежать сбоя
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            } else {
                java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
            }
        }

        val dreamRequest = DreamRequest(
            id = if (currentDreamId != -1L) currentDreamId else null,
            title = title,
            story = story,
            date = dateToSend,
            userId = currentUserId
        )

        val call = if (currentDreamId != -1L) {
            ApiClient.apiService.updateDream(currentDreamId, dreamRequest)
        } else {
            ApiClient.apiService.createDream(dreamRequest)
        }

        call.enqueue(object : Callback<Dream> {
            override fun onResponse(call: Call<Dream>, response: Response<Dream>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddEditDreamActivity, "Dream successfully saved!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@AddEditDreamActivity, "Error in saving a dream: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    Log.e("AddEditDream", "Error saving dream: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<Dream>, t: Throwable) {
                Toast.makeText(this@AddEditDreamActivity, "Network error in saving a dream: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("AddEditDream", "Network error saving dream: ${t.message}", t)
            }
        })
    }
}
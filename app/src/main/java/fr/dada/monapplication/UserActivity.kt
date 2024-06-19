package fr.dada.monapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class UserActivity : AppCompatActivity() {
    private lateinit var editUsername: EditText
    private lateinit var editNewPassword: EditText
    private lateinit var editConfirmPassword: EditText
    private lateinit var editAdminPassword: EditText
    private lateinit var buttonSave: Button
    private val client = OkHttpClient()

    private var userId: Int = 0
    private lateinit var token: String
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)

        token = sharedPreferences.getString("token",null).toString()


        if(token == null) {
            navigateTo(LobbyActivity::class.java);
            return
        }
        userId = intent.getIntExtra("user_id", 0)

        editUsername = findViewById(R.id.edit_username)
        editNewPassword = findViewById(R.id.edit_new_password)
        editConfirmPassword = findViewById(R.id.edit_confirm_password)
        editAdminPassword = findViewById(R.id.edit_admin_password)
        buttonSave = findViewById(R.id.button_save)

        buttonSave.setOnClickListener { saveUserDetails() }
    }

    private fun saveUserDetails() {
        val username = editUsername.text.toString()
        val newPassword = editNewPassword.text.toString()
        val confirmPassword = editConfirmPassword.text.toString()
        val adminPassword = editAdminPassword.text.toString()

        if (username.isNotEmpty()) {
            editStudentName(userId, username)
        }

        if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
            changeStudentPassword(userId, newPassword, adminPassword)
        } else if (newPassword != confirmPassword) {
            Snackbar.make(findViewById(android.R.id.content), "Passwords do not match", Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun editStudentName(userId: Int, username: String) {
        val url = "http://192.168.19.1/toDoList/api/admin/student-edit.php"
        val payload = JSONObject().apply {
            put("token", token)
            put("username", username)
            put("user_id",userId)
        }

        val requestBody = payload.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder().url(url).post(requestBody).build()
        client.newCall(request).enqueue(getCallbackForApiResponseEdit())
    }

    private fun getCallbackForApiResponseEdit(): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.network_error),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {

                        navigateTo(GestionUserActivity::class.java)
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Failed to edit student",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun changeStudentPassword(userId: Int, newPassword: String, adminPassword: String) {
        val url = "http://192.168.19.1/toDoList/api/admin/student-change.php"

        val payload = JSONObject().apply {
            put("user_id", userId)
            put("new_pass", newPassword)
            put("c_new_pass", newPassword)
            put("admin_pass", adminPassword)
            put("token", token)
        }

        val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("logFailure", e.toString())
                runOnUiThread {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.network_error),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        navigateTo(GestionUserActivity::class.java)
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Failed to change password",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
    private fun navigateTo(activity: Class<*>, isFinish: Boolean = true) {
        val intent = Intent(this, activity)
        startActivity(intent)
        if (isFinish) finish()
    }
}

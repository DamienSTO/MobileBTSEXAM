package fr.dada.monapplication;

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

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var boutonLogin: Button

    private lateinit var sharedPreferences: SharedPreferences

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        boutonLogin = findViewById(R.id.buttonLogin)

        boutonLogin.setOnClickListener{
            login()
        }
    }
    private fun login() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (username.isNotBlank() && password.isNotBlank()) {
            sendRequest("http://192.168.19.1/toDoList/api/login.php", username, password)
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.login_empty_field),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendRequest(url: String, username: String, password: String) {
            val payload = JSONObject().apply {
                put("username", username)
                put("password", password)
            }

            val requestBody = payload.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder().url(url).post(requestBody).build()
            client.newCall(request).enqueue(getCallbackForApiResponse())
        }

    private fun getCallbackForApiResponse(): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("logFailure",e.toString())
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
                        val responseBody = response.body?.string()
                        val jsonObject = JSONObject(responseBody)
                        val token = jsonObject.getString("token")

                        sharedPreferences.edit().putString("token", token).apply()

                        navigateTo(LobbyActivity::class.java)
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            getString(R.string.login_invalid_credentials),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun navigateTo(activity: Class<*>) {
        val intent = Intent(this, activity)
        startActivity(intent)
        finish()
    }
}


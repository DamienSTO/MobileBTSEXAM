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

class AddHostActivity : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var addUserButton: Button
    private val client = OkHttpClient()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var token: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        addUserButton = findViewById(R.id.add_user_button)

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        token = sharedPreferences.getString("token", null).toString()

        addUserButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                addUser("http://192.168.19.1/toDoList/api/admin/teacher-add.php", token, username, password)
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Veuillez remplir tous les champs",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addUser(url: String, token: String, username: String, password: String) {
        val payload = JSONObject().apply {
            put("token", token)
            put("username", username)
            put("password", password)
        }

        val requestBody = payload.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

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
                        navigateTo(GestionHostActivity::class.java)
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Échec de l'ajout de l'utilisateur",
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

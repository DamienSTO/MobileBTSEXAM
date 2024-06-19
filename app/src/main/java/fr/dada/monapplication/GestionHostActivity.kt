package fr.dada.monapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class GestionHostActivity : AppCompatActivity() {
    private lateinit var boutonPlus: ImageButton
    private lateinit var boutonUser: ImageButton
    private lateinit var searchView: SearchView
    private val client = OkHttpClient()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var token: String
    private val usersList = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hgroups)

        boutonUser = findViewById(R.id.btn_users)
        searchView = findViewById(R.id.search_view)
        boutonPlus = findViewById(R.id.btn_plus)

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        token = sharedPreferences.getString("token", null).toString()

        if (token == null) {
            navigateTo(LobbyActivity::class.java)
            return
        }

        boutonUser.setOnClickListener {
            navigateTo(GestionUserActivity::class.java)
        }

        boutonPlus.setOnClickListener {
            navigateTo(AddHostActivity::class.java)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText)
                return true
            }
        })

        fetchUsers("http://192.168.19.1/toDoList/api/admin/teacher.php", token)
    }

    private fun fetchUsers(url: String, token: String) {
        val payload = JSONObject().apply {
            put("token", token)
        }

        val requestBody = payload.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder().url(url).post(requestBody).build()
        client.newCall(request).enqueue(getUsersCallback())
    }

    private fun getUsersCallback(): Callback {
        return object : Callback {
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
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonObject = JSONObject(responseBody)
                            val usersArray = jsonObject.getJSONArray("users")
                            usersList.clear()
                            for (i in 0 until usersArray.length()) {
                                usersList.add(usersArray.getJSONObject(i))
                            }
                            displayUsers(usersList)
                        }
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            getString(R.string.fetch_users_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun filterUsers(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            usersList
        } else {
            usersList.filter {
                it.getString("username").contains(query, ignoreCase = true)
            }
        }
        displayUsers(filteredList)
    }

    private fun displayUsers(usersArray: List<JSONObject>) {
        val userContainer = findViewById<LinearLayout>(R.id.user_container)
        userContainer.removeAllViews()

        for (user in usersArray) {
            val userId = user.getInt("user_id")
            val userName = user.getString("username")

            val userLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
            }

            val userNameView = TextView(this).apply {
                text = userName
                textSize = 18f
                setPadding(16, 16, 16, 16)
            }

            val buttonsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val editButton = Button(this).apply {
                text = "Modifier"
                setTextColor(resources.getColor(android.R.color.black))
                setBackgroundResource(R.drawable.bouton_selector)
                setPadding(16, 0, 16, 0)
                setOnClickListener { onEditUser(userId) }
            }

            val deleteButton = Button(this).apply {
                text = "Supprimer"
                setTextColor(resources.getColor(android.R.color.black))
                setBackgroundResource(R.drawable.bouton_selector)
                setPadding(16, 0, 16, 0)
                setOnClickListener { onDeleteUser(userId) }
            }

            buttonsLayout.addView(editButton)
            buttonsLayout.addView(deleteButton)

            userLayout.addView(userNameView)
            userLayout.addView(buttonsLayout)

            userContainer.addView(userLayout)
        }
    }

    private fun onEditUser(userId: Int) {
        val intent = Intent(this, HostActivity::class.java)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }

    private fun onDeleteUser(userId: Int) {
        val url = "http://192.168.19.1/toDoList/api/admin/teacher-delete.php"

        val payload = JSONObject().apply {
            put("user_id", userId)
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
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "User deleted successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        fetchUsers("http://192.168.19.1/toDoList/api/admin/teacher.php", token)
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Failed to delete user",
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

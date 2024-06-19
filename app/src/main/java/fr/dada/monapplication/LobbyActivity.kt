package fr.dada.monapplication;

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LobbyActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var boutonUser: ImageButton
    private lateinit var boutonHost: ImageButton
    private lateinit var boutonLogout: ImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        boutonUser = findViewById(R.id.btn_users)
        boutonHost = findViewById(R.id.btn_group_leader)
        boutonLogout = findViewById(R.id.btn_logout)

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)

        val token = sharedPreferences.getString("token",null)

        if(token == null) {
            NavigateTo(LobbyActivity::class.java);
            return
        }

        checkToken("http://192.168.19.1/toDoList/api/check-token.php",token)

        boutonUser.setOnClickListener{
            NavigateTo(GestionUserActivity::class.java,false)
        }
        boutonHost.setOnClickListener{
            NavigateTo(GestionHostActivity::class.java,false)
        }
        boutonLogout.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            NavigateTo(LoginActivity::class.java)
        }
    }






    private fun checkToken(url: String, token: String) {
        val payload = JSONObject().apply {
            put("token", token)
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
                    if (!response.isSuccessful) {
                        NavigateTo(LoginActivity::class.java)
                    }
                }
            }
        }
    }
    private fun NavigateTo(activity:Class<*>, isFinish:Boolean = true){
        val intent= Intent(this,activity)
        startActivity(intent)
        if (isFinish) {
            finish()
        }
    }

}

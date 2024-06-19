package fr.dada.monapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton


class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)

        val token = sharedPreferences.getString("token",null)

        if(token !=null) {

            NavigateTo(LobbyActivity::class.java)

            return
        }

        val login_bouton=findViewById<AppCompatButton>(R.id.login_bouton)
        login_bouton.setOnClickListener {
            Toast.makeText(this, "bouton_clicker", Toast.LENGTH_SHORT).show()
            NavigateTo(LoginActivity::class.java)
        }
    }
    private fun NavigateTo(activity:Class<*>) {
        val intent= Intent(this,activity)
        startActivity(intent)
        finish()
    }



}


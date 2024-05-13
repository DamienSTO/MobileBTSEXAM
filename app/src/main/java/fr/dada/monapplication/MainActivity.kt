package fr.dada.monapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import fr.dada.monapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)

        val token = sharedPreferences.getString("token",null)

        if(token !=null) {

            NavigateTo(LoggedActivity::class.java)

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
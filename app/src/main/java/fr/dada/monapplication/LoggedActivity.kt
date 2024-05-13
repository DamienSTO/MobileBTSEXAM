package fr.dada.monapplication;

import android.app.Activity;
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LoggedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

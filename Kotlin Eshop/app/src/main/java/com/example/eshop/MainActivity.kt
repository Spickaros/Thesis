package com.example.eshop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optional: you can use a blank layout or skip setContentView entirely,
        // but some devices may require a layout. For safety:
        setContentView(R.layout.activity_main) // blank layout with just a ProgressBar or empty

        // Navigate to LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))

        // Finish so MainActivity isn't in the back stack
        finish()
    }
}


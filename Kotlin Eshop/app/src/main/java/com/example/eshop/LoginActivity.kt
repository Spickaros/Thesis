package com.example.eshop
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLink = findViewById<TextView>(R.id.registerLink)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            val startTime = System.currentTimeMillis() // ⏱ Start timing

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val endTime = System.currentTimeMillis()
                    val latency = endTime - startTime

                    Toast.makeText(this, "Login successful in $latency ms", Toast.LENGTH_SHORT).show()
                    // Optionally log or store this latency
                    Log.d("AuthLatency", "Login latency: ${latency}ms")

                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    val endTime = System.currentTimeMillis()
                    val latency = endTime - startTime

                    Toast.makeText(this, "Login failed (${latency}ms): ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.d("AuthLatency", "Login failed latency: ${latency}ms")
                }
        }


        registerLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}

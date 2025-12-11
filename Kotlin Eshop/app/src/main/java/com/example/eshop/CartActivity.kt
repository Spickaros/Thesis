package com.example.eshop

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var adapter: CartAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.cartRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val cartItems = CartManager.getCart()
        adapter = CartAdapter(cartItems)
        recyclerView.adapter = adapter

        val total = cartItems.sumOf { it.price }
        totalTextView.text = "Total: $${String.format("%.2f", total)}"
        val placeOrderButton = findViewById<Button>(R.id.place_order_button)

        placeOrderButton.setOnClickListener {
            val cartItems = CartManager.getCart()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId == null) {
                Toast.makeText(this, "Please log in to place an order", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val order = mapOf(
                "userId" to userId,
                "timestamp" to System.currentTimeMillis(),
                "items" to cartItems.map {
                    mapOf(
                        "productId" to it.product_id,
                        "title" to it.name,
                        "price" to it.price,
                        "image" to it.image,
                        "quantity" to 1 // You can expand this later
                    )


                }
            )

            val dbRef = FirebaseDatabase.getInstance().getReference("orders").push()
            dbRef.setValue(order)
                .addOnSuccessListener {
                    Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show()
                    CartManager.clearCart()
                    finish() // Close cart screen or redirect
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show()
                }

            val ordersRef = FirebaseDatabase.getInstance().getReference("orders")

            val startTime = System.currentTimeMillis()

            ordersRef.push().setValue(order)
                .addOnSuccessListener {
                    val endTime = System.currentTimeMillis()
                    val latency = endTime - startTime
                    Log.d("Latency", "Order save latency: ${latency}ms")

                    Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show()
                }

        }

    }
}
package com.example.eshop

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ProductDetailsActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_product_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                startActivity(Intent(this, CartActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Get data from intent
        val id = intent.getLongExtra("product_id", 0L)
        val name = intent.getStringExtra("product_name")
        val description = intent.getStringExtra("product_description")
        val price = intent.getDoubleExtra("product_price", 0.0)
        val image = intent.getStringExtra("product_image")


// Set text
        findViewById<TextView>(R.id.product_name).text = name
        findViewById<TextView>(R.id.product_description).text = description
        findViewById<TextView>(R.id.product_price).text = "$${price}"

        val imageView = findViewById<ImageView>(R.id.product_image)
        Glide.with(this).load(image).into(imageView)

        Log.d("ProductDetails", "Image from Intent: $image")

// Add to cart
        findViewById<Button>(R.id.add_to_cart_button).setOnClickListener {
            val product = Product(
                product_id = id,
                name = name ?: "",
                price = price,
                description = description ?: "",
                image = image ?: ""

            )
            CartManager.addToCart(product)
            Toast.makeText(this, "$name added to cart", Toast.LENGTH_SHORT).show()
        }

    }


}

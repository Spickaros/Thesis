package com.example.eshop

import ProductAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var productList: ArrayList<Product>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    // Add this at the top with other declarations
    private lateinit var categorySpinner: Spinner
    private lateinit var allProducts: ArrayList<Product> // To store unfiltered list


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<TextView>(R.id.myOrdersLink).setOnClickListener {
            startActivity(Intent(this, MyOrdersActivity::class.java))

        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = ArrayList()

        // ✅ Initialize adapter with click listener BEFORE setting to RecyclerView
        adapter = ProductAdapter(productList) { product ->
            val intent = Intent(this, ProductDetailsActivity::class.java).apply {
                putExtra("product_id", product.product_id)
                putExtra("product_name", product.name)
                putExtra("product_description", product.description)
                putExtra("product_price", product.price)
                putExtra("product_image", product.image) // ✅ Add this line

            }

            startActivity(intent)
        }

        recyclerView.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("products")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnap in snapshot.children) {
                    val product = productSnap.getValue(Product::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
        categorySpinner = findViewById(R.id.categorySpinner)
        Log.d("Firebase", "Fetched ${productList.size} products")
        for (p in productList) {
            Log.d("FirebaseProduct", "Title: ${p.name}, Image: ${p.image}")
        }


// Set up spinner items
        val categories = listOf("All", "men's clothing", "jewelery", "electronics", "women's clothing")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                filterProducts(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        allProducts = ArrayList()

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProducts.clear()
                for (productSnap in snapshot.children) {
                    val product = productSnap.getValue(Product::class.java)
                    if (product != null) {
                        allProducts.add(product)
                    }
                }
                allProducts.clear()
                for (productSnap in snapshot.children) {
                    val product = productSnap.getValue(Product::class.java)
                    if (product != null) {
                        allProducts.add(product)
                    }
                }
                filterProducts(categorySpinner.selectedItem.toString())


            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Latency", "Product fetch failed")
            }
        })


    }
    private fun filterProducts(category: String) {
        productList.clear()
        if (category == "All") {
            productList.addAll(allProducts)
        } else {
            val filtered = allProducts.filter { it.category == category }
            productList.addAll(filtered)
        }
        adapter.notifyDataSetChanged()
    }

}


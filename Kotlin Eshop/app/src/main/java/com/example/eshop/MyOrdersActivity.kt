package com.example.eshop

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrdersAdapter
    private val productList = mutableListOf<OrderProduct>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        recyclerView = findViewById(R.id.ordersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = OrdersAdapter(productList)
        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please log in to view orders", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("orders")
        dbRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productList.clear()
                    for (orderSnap in snapshot.children) {
                        val items = orderSnap.child("items").children
                        for (itemSnap in items) {
                            val title = itemSnap.child("title").getValue(String::class.java) ?: ""
                            val price = itemSnap.child("price").getValue(Double::class.java) ?: 0.0
                            val image = itemSnap.child("image").getValue(String::class.java)

                            productList.add(OrderProduct(title, price, image))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyOrdersActivity, "Failed to load orders", Toast.LENGTH_SHORT).show()
                }
            })
    }
}


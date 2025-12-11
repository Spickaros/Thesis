package com.example.eshop

data class Product(
    val product_id: Long = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val unit: String = "",
    val image: String = "",
    val discount: Int = 0,
    val availability: Boolean = true,
    val brand: String = "",
    val category: String = "",
    val rating: Double = 0.0
)

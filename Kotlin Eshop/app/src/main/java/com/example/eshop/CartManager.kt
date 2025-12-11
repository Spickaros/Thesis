package com.example.eshop

object CartManager {
    private val cartItems = mutableListOf<Product>()

    fun addToCart(product: Product) {
        cartItems.add(product)
    }

    fun getCart(): List<Product> = cartItems
    fun clearCart() {
        cartItems.clear()
    }


}


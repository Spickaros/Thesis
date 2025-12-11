package com.example.eshop

import android.content.Context
import android.widget.Toast
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

fun uploadProductsFromJson(context: Context) {
    val database = FirebaseDatabase.getInstance().getReference("products")

    val jsonStr = context.assets.open("products.json")
        .bufferedReader()
        .use { it.readText() }

    val gson = Gson()
    val type = object : TypeToken<List<Map<String, Any>>>() {}.type
    val productList: List<Map<String, Any>> = gson.fromJson(jsonStr, type)

    database.setValue(productList)
        .addOnSuccessListener {
            Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_LONG).show()
        }

}

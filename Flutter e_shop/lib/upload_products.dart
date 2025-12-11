import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:firebase_database/firebase_database.dart';

Future<void> uploadProductsFromJson() async {
  final String response = await rootBundle.loadString('assets/products.json');
  final List<dynamic> productList = json.decode(response);

  final DatabaseReference ref = FirebaseDatabase.instance.ref("products");

  for (var product in productList) {
    await ref.push().set(product);
  }

  print("✅ Products uploaded to Firebase Realtime Database");
}

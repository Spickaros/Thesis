import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_database/firebase_database.dart';
import 'home_screen.dart'; // To access the Product model

class CartScreen extends StatefulWidget {
  final List<Product> cart;

  const CartScreen({super.key, required this.cart});

  @override
  State<CartScreen> createState() => _CartScreenState();
}

class _CartScreenState extends State<CartScreen> {
  bool _isPlacingOrder = false;

  double get totalPrice =>
      widget.cart.fold(0.0, (sum, product) => sum + product.price);

  Future<void> _placeOrder() async {
    final user = FirebaseAuth.instance.currentUser;
    if (user == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('You must be logged in to place an order'),
        ),
      );
      return;
    }

    setState(() => _isPlacingOrder = true);

    final stopwatch = Stopwatch()..start(); // Start measuring

    try {
      final orderRef =
          FirebaseDatabase.instance
              .ref()
              .child('orders')
              .child(user.uid)
              .push();

      final orderData = {
        'timestamp': DateTime.now().toIso8601String(),
        'items':
            widget.cart.map((product) {
              return {
                'title': product.name,
                'price': product.price,
                'category': product.category,
                'image': product.image,
              };
            }).toList(),
        'total': totalPrice,
      };

      await orderRef.set(orderData);

      stopwatch.stop(); // Stop measuring
      print('🕒 Order placed in ${stopwatch.elapsedMilliseconds} ms');

      setState(() {
        widget.cart.clear();
        _isPlacingOrder = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Order placed successfully!')),
      );

      Navigator.pop(context);
    } catch (e) {
      stopwatch.stop(); // Also stop if there's an error
      print('❌ Failed in ${stopwatch.elapsedMilliseconds} ms');

      setState(() => _isPlacingOrder = false);
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Failed to place order: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final cart = widget.cart;

    return Scaffold(
      appBar: AppBar(title: const Text('Cart')),
      body:
          cart.isEmpty
              ? const Center(child: Text('Your cart is empty'))
              : ListView.builder(
                itemCount: cart.length,
                itemBuilder: (ctx, i) {
                  final item = cart[i];
                  return ListTile(
                    leading: Image.network(
                      item.image,
                      width: 60,
                      height: 60,
                      fit: BoxFit.cover,
                      errorBuilder:
                          (_, __, ___) => const Icon(Icons.broken_image),
                    ),
                    title: Text(item.name),
                    subtitle: Text('Category: ${item.category}'),
                    trailing: Text('\$${item.price.toStringAsFixed(2)}'),
                  );
                },
              ),
      bottomNavigationBar:
          cart.isEmpty
              ? null
              : Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const SizedBox(height: 12),
                    ElevatedButton(
                      onPressed: _isPlacingOrder ? null : _placeOrder,
                      child:
                          _isPlacingOrder
                              ? const CircularProgressIndicator(
                                color: Colors.white,
                              )
                              : const Text('Place Order'),
                    ),
                  ],
                ),
              ),
    );
  }
}

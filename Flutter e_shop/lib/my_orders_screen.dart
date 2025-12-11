import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';

class Product {
  final String category;
  final String image;
  final double price;
  final String title;

  Product({
    required this.category,
    required this.image,
    required this.price,
    required this.title,
  });

  factory Product.fromJson(Map<String, dynamic> json) {
    return Product(
      category: json['category'] ?? '',
      image: json['image'] ?? '',
      price: (json['price'] ?? 0).toDouble(),
      title: json['title'] ?? '',
    );
  }
}

class Order {
  final String orderId;
  final List<Product> items;
  final double totalPrice;
  final String status;
  final String date;

  Order({
    required this.orderId,
    required this.items,
    required this.totalPrice,
    required this.status,
    required this.date,
  });

  factory Order.fromJson(String orderId, Map<dynamic, dynamic> json) {
    List<Product> products = [];

    if (json['items'] != null) {
      var itemsData = json['items'];
      if (itemsData is List) {
        products =
            itemsData
                .where((item) => item != null)
                .map(
                  (item) => Product.fromJson(Map<String, dynamic>.from(item)),
                )
                .toList();
      } else if (itemsData is Map) {
        products =
            itemsData.values
                .map(
                  (item) => Product.fromJson(Map<String, dynamic>.from(item)),
                )
                .toList();
      }
    }

    return Order(
      orderId: orderId,
      items: products,
      totalPrice: (json['totalPrice'] ?? 0).toDouble(),
      status: json['status'] ?? 'pending',
      date: json['date'] ?? '',
    );
  }
}

class MyOrdersScreen extends StatefulWidget {
  final String userId;

  const MyOrdersScreen({Key? key, required this.userId}) : super(key: key);

  @override
  State<MyOrdersScreen> createState() => _MyOrdersScreenState();
}

class _MyOrdersScreenState extends State<MyOrdersScreen> {
  late Future<List<Order>> _futureOrders;

  @override
  void initState() {
    super.initState();
    _futureOrders = fetchOrders(widget.userId);
  }

  Future<List<Order>> fetchOrders(String userId) async {
    final ref = FirebaseDatabase.instance.ref().child('orders').child(userId);
    final snapshot = await ref.get();

    if (snapshot.exists) {
      final data = snapshot.value;
      if (data == null) return [];

      if (data is Map) {
        return data.entries.map((entry) {
          final orderId = entry.key;
          final orderData = Map<dynamic, dynamic>.from(entry.value);
          return Order.fromJson(orderId, orderData);
        }).toList();
      } else {
        throw Exception('Unexpected orders data format.');
      }
    } else {
      return [];
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('My Orders')),
      body: FutureBuilder<List<Order>>(
        future: _futureOrders,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return const Center(child: Text('No orders found.'));
          }

          final orders = snapshot.data!;
          return ListView.builder(
            itemCount: orders.length,
            itemBuilder: (context, index) {
              final order = orders[index];
              return Card(
                margin: const EdgeInsets.all(10),
                child: ListTile(
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: 8),
                      ...order.items.map(
                        (item) => Padding(
                          padding: const EdgeInsets.symmetric(vertical: 2),
                          child: Row(
                            children: [
                              Image.network(
                                item.image,
                                width: 40,
                                height: 40,
                                fit: BoxFit.cover,
                              ),
                              const SizedBox(width: 8),
                              Expanded(child: Text(item.title)),
                              Text('\$${item.price.toStringAsFixed(2)}'),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                  isThreeLine: true,
                ),
              );
            },
          );
        },
      ),
    );
  }
}

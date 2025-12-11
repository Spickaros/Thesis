import 'package:flutter/material.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'product_details_screen.dart';
import 'cart_screen.dart';
import 'my_orders_screen.dart';
import 'package:firebase_performance/firebase_performance.dart';
import 'package:firebase_auth/firebase_auth.dart';

class Product {
  final String category;
  final String image;
  final double price;
  final String name;
  final String description; // Optional

  Product({
    required this.category,
    required this.image,
    required this.price,
    required this.name,
    required this.description,
  });

  factory Product.fromJson(Map<String, dynamic> json) {
    return Product(
      category: json['category'] ?? '',
      image: json['image'] ?? '',
      price: (json['price'] ?? 0).toDouble(),
      name: json['name'] ?? '',
      description: json['description'] ?? '',
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late Future<List<Product>> _futureProducts;
  List<Product> _allProducts = [];
  List<Product> _filteredProducts = [];
  final List<Product> _cart = [];

  String _selectedCategory = 'All';

  @override
  void initState() {
    super.initState();
    _futureProducts = fetchProducts();
    _futureProducts.then((products) {
      setState(() {
        _allProducts = products;
        _filteredProducts = products;
      });
    });
  }

  Future<List<Product>> fetchProducts() async {
    final stopwatch = Stopwatch()..start();

    final ref = FirebaseDatabase.instance.ref().child('products');
    final snapshot = await ref.get();

    stopwatch.stop();
    print('Fetch products took: ${stopwatch.elapsedMilliseconds} ms');
    if (snapshot.exists) {
      final data = snapshot.value;
      if (data == null) return [];

      if (data is Map) {
        return data.entries.map((entry) {
          final productData = Map<String, dynamic>.from(entry.value);
          return Product.fromJson(productData);
        }).toList();
      } else if (data is List) {
        return data
            .whereType<Map>()
            .map((item) => Product.fromJson(Map<String, dynamic>.from(item)))
            .toList();
      } else {
        throw Exception('Unexpected products data format.');
      }
    } else {
      return [];
    }
  }

  void _filterByCategory(String category) {
    setState(() {
      _selectedCategory = category;
      if (category == 'All') {
        _filteredProducts = _allProducts;
      } else {
        _filteredProducts =
            _allProducts.where((p) => p.category == category).toList();
      }
    });
  }

  List<String> _getCategories() {
    final categories = _allProducts.map((p) => p.category).toSet().toList();
    categories.sort();
    categories.insert(0, 'All');
    return categories;
  }

  void _showFilterDialog() {
    final categories = _getCategories();
    String tempSelected = _selectedCategory;

    showDialog(
      context: context,
      builder:
          (context) => AlertDialog(
            title: const Text('Filter by Category'),
            content: StatefulBuilder(
              builder: (context, setState) {
                return DropdownButton<String>(
                  value: tempSelected,
                  isExpanded: true,
                  items:
                      categories.map((cat) {
                        return DropdownMenuItem(value: cat, child: Text(cat));
                      }).toList(),
                  onChanged: (value) {
                    if (value != null) {
                      setState(() {
                        tempSelected = value;
                      });
                    }
                  },
                );
              },
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Cancel'),
              ),
              ElevatedButton(
                onPressed: () {
                  _filterByCategory(tempSelected);
                  Navigator.pop(context);
                },
                child: const Text('Apply'),
              ),
            ],
          ),
    );
  }

  void _goToCart() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => CartScreen(cart: _cart)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('All Products'),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            tooltip: 'Filter by category',
            onPressed: _showFilterDialog,
          ),
          IconButton(
            icon: Icon(Icons.logout),
            onPressed: () async {
              await FirebaseAuth.instance.signOut();
              Navigator.of(
                context,
              ).pushReplacementNamed('/login'); // or use Navigator.push
            },
          ),
          TextButton(
            onPressed: () {
              final userId = FirebaseAuth.instance.currentUser?.uid ?? '';
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => MyOrdersScreen(userId: userId),
                ),
              );
            },
            child: const Text(
              'My Orders',
              style: TextStyle(
                color: Colors.black, // Make sure text is visible in AppBar
                fontSize: 16,
              ),
            ),
          ),
        ],
      ),
      body: FutureBuilder<List<Product>>(
        future: _futureProducts,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return const Center(child: Text('No products found.'));
          }

          // Use _filteredProducts for display since it’s filtered
          if (_filteredProducts.isEmpty) {
            return const Center(
              child: Text('No products found for this category.'),
            );
          }

          return GridView.builder(
            padding: const EdgeInsets.all(10),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              childAspectRatio: 3 / 4,
              crossAxisSpacing: 10,
              mainAxisSpacing: 10,
            ),
            itemCount: _filteredProducts.length,
            itemBuilder: (context, index) {
              final product = _filteredProducts[index];
              return GestureDetector(
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder:
                          (_) => ProductDetailsScreen(
                            product: product,
                            cart: _cart,
                          ),
                    ),
                  );
                },
                child: Card(
                  elevation: 2,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Expanded(
                        child: Image.network(product.image, fit: BoxFit.cover),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Text(
                          product.name,
                          style: const TextStyle(fontWeight: FontWeight.bold),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 8.0),
                        child: Text('\$${product.price.toStringAsFixed(2)}'),
                      ),
                      const SizedBox(height: 8),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _goToCart,
        child: const Icon(Icons.shopping_cart),
        tooltip: 'Go to Cart',
      ),
    );
  }
}

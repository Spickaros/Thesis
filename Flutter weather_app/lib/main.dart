import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:http/http.dart' as http;
import 'package:latlong2/latlong.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: WeatherMapApp());
  }
}

class WeatherMapApp extends StatefulWidget {
  @override
  _WeatherMapAppState createState() => _WeatherMapAppState();
}

class _WeatherMapAppState extends State<WeatherMapApp> {
  final MapController _mapController = MapController();
  LatLng _center = LatLng(20.0, 0.0);
  String _city = '';
  Map<String, dynamic>? _weatherData;

  Future<void> _searchAndFetchWeather(String city) async {
    const apiKey = '4564573f304c4eea0a7aa5da9fed9074';

    final geoUrl = Uri.parse(
      'http://api.openweathermap.org/geo/1.0/direct?q=$city&limit=1&appid=$apiKey',
    );
    final geoRes = await http.get(geoUrl);

    if (geoRes.statusCode != 200) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("Failed to fetch location")));
      return;
    }

    final geoJson = jsonDecode(geoRes.body);

    if (geoJson.isNotEmpty) {
      final lat = geoJson[0]['lat'];
      final lon = geoJson[0]['lon'];
      final newCenter = LatLng(lat, lon);

      final weatherUrl = Uri.parse(
        'https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$apiKey',
      );
      final weatherRes = await http.get(weatherUrl);

      setState(() {
        _center = newCenter;
        _weatherData = jsonDecode(weatherRes.body);
      });

      _mapController.move(_center, 10.0);
    } else {
      setState(() => _weatherData = null);
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text("City not found")));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          FlutterMap(
            mapController: _mapController,
            options: MapOptions(initialCenter: _center, initialZoom: 10.0),
            children: [
              TileLayer(
                urlTemplate:
                    'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                subdomains: ['a', 'b', 'c'],
              ),
              if (_weatherData != null)
                MarkerLayer(
                  markers: [
                    Marker(
                      point: _center,
                      child: Container(
                        child: Image.network(
                          'http://openweathermap.org/img/wn/${_weatherData!['weather'][0]['icon']}@2x.png',
                          width: 40,
                          height: 40,
                        ),
                      ),
                    ),
                  ],
                ),
            ],
          ),

          Positioned(
            top: 40,
            left: 16,
            right: 16,
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    decoration: InputDecoration(labelText: 'Enter city name'),
                    onChanged: (value) => _city = value,
                  ),
                ),
                IconButton(
                  icon: Icon(Icons.search),
                  onPressed: () => _searchAndFetchWeather(_city),
                ),
              ],
            ),
          ),

          if (_weatherData != null)
            Positioned(
              bottom: 80,
              left: 16,
              right: 16,
              child: Align(
                alignment: Alignment.center,
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 8.0,
                    vertical: 12.0,
                  ),
                  decoration: BoxDecoration(
                    color: const Color.fromARGB(
                      255,
                      192,
                      190,
                      190,
                    ).withOpacity(0.7),
                    borderRadius: BorderRadius.circular(12),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black26,
                        blurRadius: 8.0,
                        offset: Offset(0, 4),
                      ),
                    ],
                  ),
                  constraints: BoxConstraints(maxWidth: 180),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Image.network(
                        'http://openweathermap.org/img/wn/${_weatherData!['weather'][0]['icon']}@2x.png',
                        width: 30,
                        height: 30,
                      ),
                      SizedBox(width: 10),
                      Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '${_weatherData!['name']}',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                          Text(
                            '${_weatherData!['main']['temp']} °C',
                            style: TextStyle(color: Colors.white),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

import 'dart:convert';

import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:http/http.dart' as http;

String _ip = dotenv.env['IP']!;
String _port = "8000";

Future<Map<String, dynamic>> getWeatherNow(String city) async {
  final String serverUrl = 'http://$_ip:$_port/weather/city/all';

  try {
    final response = await http.post(
      Uri.parse(serverUrl),
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
      },
      body: {"city": city},
    );

    if (response.statusCode == 200) {
      final decoded = jsonDecode(response.body);
      return decoded;
    }
  } catch (e) {
    throw Exception('Failed to load weather data');
  }
  throw Exception('Failed to load weather data');
}

Future<Map<String, dynamic>> getTodayForecast(String city) async {
  final String serverUrl = "http://$_ip:$_port/weather/city/today_tomorrow_forecast";

  try {
    final response = await http.post(
      Uri.parse(serverUrl),
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
      },
      body: {"city": city}
    );

    if (response.statusCode == 200) {
      final decoded = jsonDecode(response.body);
      return decoded;
    }
  } catch (e) {
    throw Exception('Failed to load weather data');
  }
  throw Exception('Failed to load weather data');
}

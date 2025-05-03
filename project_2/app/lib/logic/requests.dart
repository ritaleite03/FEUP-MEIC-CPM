import 'dart:convert';
import 'package:http/http.dart' as http;

String _ip = "192.168.68.136";
String _port = "8000";

Future<void> getWeatherNow(String city) async {
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
      final Map<String, dynamic> data = json.decode(response.body);
    } else {
      // error
    }
  } catch (e) {
    // error
  }
}

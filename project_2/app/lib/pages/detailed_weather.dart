import 'package:app/pages/widgets/weather/details.dart';
import 'package:app/pages/widgets/weather/header.dart';
import 'package:app/pages/widgets/weather/main_content.dart';
import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';

class DetailedWeatherPage extends StatelessWidget {
  final String city;
  final Map<String, dynamic> data;
  final SpriteSheet spriteSheet;
  final Map<String, int> metrics;

  const DetailedWeatherPage({
    super.key,
    required this.city,
    required this.data,
    required this.spriteSheet,
    required this.metrics
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Scaffold(
      backgroundColor: colorScheme.primary,
      appBar: AppBar(
        title: const Text('Air Conditions'),
        backgroundColor: colorScheme.primary,
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            WeatherHeader(city: city, rainChance: data["precipitation"]["proba"].toString()),
            SizedBox(height: 75),
            WeatherMain(icon: data["info"]["icon"], temperature: data["temperature"]["realNow"].toString(), spriteSheet: spriteSheet, temperatureMetric: metrics["temperature"]!),
            const SizedBox(height: 32),
            AirConditionsDetails(
              data: data,
              metrics: metrics
            ),
          ],
        ),
      ),
    );
  }
}

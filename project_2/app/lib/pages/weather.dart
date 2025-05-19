import 'dart:async';

import 'package:app/pages/widgets/weather/conditions.dart';
import 'package:flame/components.dart';
import 'package:flame/flame.dart';
import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';

import 'package:app/logic/requests.dart';
import 'package:app/pages/widgets/utils.dart';
import 'package:app/pages/widgets/weather/header.dart';
import 'package:app/pages/widgets/weather/main_content.dart';
import 'package:app/pages/week.dart';

class WeatherPage extends StatefulWidget {
  final String? cityName;

  const WeatherPage({super.key, this.cityName});

  @override
  State<WeatherPage> createState() => _WeatherPageState();
}

class _WeatherPageState extends State<WeatherPage> {
  late Future<Map<String, dynamic>> data;
  late Future<SpriteSheet> spriteSheetFuture;
  String selected = "today";

  @override
  void initState() {
    super.initState();
    data = getWeatherNow(widget.cityName ?? 'Unknown');
    spriteSheetFuture = loadSpriteSheet();
  }

  Future<SpriteSheet> loadSpriteSheet() async {
    final image = await Flame.images.load('icon.png');
    return SpriteSheet(
      image: image,
      srcSize: Vector2(594.0 / 4.0, 792.0 / 6.0),
    );
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Scaffold(
      backgroundColor: colorScheme.primary,
      appBar: AppBarWidget(title: widget.cityName ?? ''),
      body: FutureBuilder<Map<String, dynamic>>(
        future: data,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return _buildLoadingIndicator(colorScheme);
          } else if (snapshot.hasData) {
            final today = snapshot.data!["today"];
            final tomorrow = snapshot.data!["tomorrow"];
            final week = snapshot.data!["week"];
            final selectedData = selected == "today" ? today : tomorrow;

            return FutureBuilder<SpriteSheet>(
              future: spriteSheetFuture,
              builder: (context, spriteSnapshot) {
                if (spriteSnapshot.connectionState == ConnectionState.waiting) {
                  return _buildLoadingIndicator(colorScheme);
                } else if (spriteSnapshot.hasData) {
                  return _buildWeatherContent(
                    cityName: widget.cityName!,
                    colorScheme: colorScheme,
                    today: today,
                    tomorrow: tomorrow,
                    week: week,
                    selectedData: selectedData,
                    spriteSheet: spriteSnapshot.data!,
                  );
                } else {
                  return const Center(child: Text("Error loading icons"));
                }
              },
            );
          } else {
            return const Center(child: Text("No weather data available"));
          }
        },
      ),
    );
  }

  Widget _buildLoadingIndicator(ColorScheme colorScheme) {
    return Center(
      child: CircularProgressIndicator(color: colorScheme.primaryContainer),
    );
  }

  Widget _buildWeatherContent({
    required String cityName,
    required ColorScheme colorScheme,
    required Map<String, dynamic> today,
    required Map<String, dynamic> tomorrow,
    required List<dynamic> week,
    required Map<String, dynamic> selectedData,
    required SpriteSheet spriteSheet,
  }) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: SingleChildScrollView(
        child: Center(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              WeatherHeader(city: cityName, rainChance: selectedData["precipitation"]["proba"]),
              SizedBox(height: 75),
              WeatherMain(icon: selectedData["info"]["icon"], temperature: selectedData["temperature"]["realNow"], spriteSheet: spriteSheet),
              SizedBox(height: 20),
              WeatherConditions(data: selectedData),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity, // ocupa toda a largura possível
                child: ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder:
                            (context) => WeekPage(
                              cityName: widget.cityName,
                              today: today,
                              week: week,
                            ),
                      ),
                    );
                  },
                  child: const Text("See past week weather..."),
                ),
              ),
            ]
          ),
        ),
      ),
    );
  }
}

import 'dart:async';
import 'package:app/logic/requests.dart';
import 'package:app/pages/widgets/utils.dart';
import 'package:app/pages/widgets/weather/buttons_widget.dart';
import 'package:app/pages/widgets/weather/day_widget.dart';
import 'package:app/pages/widgets/weather/week_widget.dart';
import 'package:flame/flame.dart';
import 'package:flame/components.dart';
import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';

class WeatherPage extends StatefulWidget {
  final String? cityName;
  const WeatherPage({super.key, this.cityName});

  @override
  State<WeatherPage> createState() => _WeatherPageState();
}

class _WeatherPageState extends State<WeatherPage> {
  late Future<Map<String, dynamic>> data;
  late Future<SpriteSheet> spriteSheetFuture;

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
            return Center(
              child: CircularProgressIndicator(
                color: colorScheme.primaryContainer,
              ),
            );
          } else if (snapshot.hasData) {
            final today = snapshot.data!["today"];
            // final tomorrow = snapshot.data!["tomorrow"];
            final week = snapshot.data!["week"];
            return FutureBuilder<SpriteSheet>(
              future: spriteSheetFuture,
              builder: (context, spriteSnapshot) {
                if (spriteSnapshot.connectionState == ConnectionState.waiting) {
                  return Center(
                    child: CircularProgressIndicator(
                      color: colorScheme.primaryContainer,
                    ),
                  );
                } else if (spriteSnapshot.hasData) {
                  return Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: SingleChildScrollView(
                      child: Column(
                        children: [
                          DayWidget(
                            data: today,
                            icon: today["info"]["icon"],
                            spriteSheet: spriteSnapshot.data!,
                          ),
                          ButtonsWidget(data: today),
                          WeekWidget(today: today, week: week),
                        ],
                      ),
                    ),
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
}

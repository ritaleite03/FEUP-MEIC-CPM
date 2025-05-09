import 'dart:async';
import 'dart:math';

import 'package:flame/components.dart';
import 'package:flame/flame.dart';
import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';

import 'package:app/logic/requests.dart';
import 'package:app/pages/widgets/utils.dart';
import 'package:app/pages/widgets/weather/day_details.dart';
import 'package:app/pages/widgets/weather/day_header.dart';
import 'package:app/pages/widgets/weather/day_main.dart';
import 'package:app/pages/week.dart';

double _directionToAngle(String dir) {
  switch (dir.toUpperCase()) {
    case 'N':
      return 0;
    case 'NE':
      return pi / 4;
    case 'E':
      return pi / 2;
    case 'SE':
      return 3 * pi / 4;
    case 'S':
      return pi;
    case 'SW':
      return 5 * pi / 4;
    case 'W':
      return 3 * pi / 2;
    case 'NW':
      return 7 * pi / 4;
    default:
      return 0;
  }
}

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
        child: Column(
          children: [
            DayHeader(
              selected: selected,
              onToggle: () {
                setState(() {
                  selected = selected == "today" ? "tomorrow" : "today";
                });
              },
            ),
            const SizedBox(height: 16),
            DayMain(
              data: selectedData,
              icon: selectedData["info"]["icon"],
              spriteSheet: spriteSheet,
            ),
            const SizedBox(height: 16),
            DayDetails(
              data: selectedData,
              labels: ["Temperature", "Precipitation", "Wind"],
              icons: [Icons.thermostat, Icons.water_drop, Icons.air],
              widgets: [
                Text(
                  "${selectedData["temperature"]["realMax"]} F / ${selectedData["temperature"]["realMin"]} F",
                  style: TextStyle(fontSize: 10, color: colorScheme.primary),
                ),
                Text(
                  "${selectedData["precipitation"]["dimen"]} - ${selectedData["precipitation"]["proba"]}%",
                  style: TextStyle(fontSize: 10, color: colorScheme.primary),
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      "${selectedData["wind"]["spd"]} km/h",
                      style: TextStyle(
                        fontSize: 10,
                        color: colorScheme.primary,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Transform.rotate(
                      angle: _directionToAngle(selectedData["wind"]["dir"]),
                      child: Icon(
                        Icons.arrow_upward,
                        size: 10,
                        color: colorScheme.primary,
                      ),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 16),
            DayDetails(
              data: selectedData,
              labels: ["Sun", "Pressure", "Humidity"],
              icons: [Icons.sunny, Icons.compress, Icons.water],
              widgets: [
                Text(
                  "${selectedData["sun"]["rad"]} - ${selectedData["sun"]["uvi"]} UIV",
                  style: TextStyle(fontSize: 10, color: colorScheme.primary),
                ),
                Text(
                  "${selectedData["pressure"]["pres"]}",
                  style: TextStyle(fontSize: 10, color: colorScheme.primary),
                ),
                Text(
                  "${selectedData["humidity"]["hum"]}%",
                  style: TextStyle(fontSize: 10, color: colorScheme.primary),
                ),
              ],
            ),
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
                style: ButtonStyle(
                  backgroundColor: WidgetStateProperty.all(Colors.amber),
                  foregroundColor: WidgetStateProperty.all(colorScheme.primary),
                ),
                child: const Text("See past week weather..."),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

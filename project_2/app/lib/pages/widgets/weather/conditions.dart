import 'package:app/pages/detailed_weather.dart';
import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';
import 'dart:math';

class WeatherConditions extends StatelessWidget {
  final String? cityName;
  final Map<String, dynamic> data;
  final SpriteSheet spriteSheet;
  final Map<String, int> metrics;

  const WeatherConditions({
    super.key,
    required this.cityName,
    required this.data,
    required this.spriteSheet,
    required this.metrics,
  });

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

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    final temperatureSymbol = metrics["temperature"] == 0 ? "º" : "F";
    final windSymbol =
        metrics["wind"] == 0
            ? "km/h"
            : (metrics["wind"] == 1 ? "m/s" : "Knots");

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colorScheme.surface,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                "Air Conditions",
                style: TextStyle(
                  color: Colors.grey,
                  fontWeight: FontWeight.bold,
                ),
              ),
              ElevatedButton(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder:
                          (context) => DetailedWeatherPage(
                            city: cityName ?? '',
                            data: data,
                            spriteSheet: spriteSheet,
                            metrics: metrics,
                          ),
                    ),
                  );
                },
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 40,
                    vertical: 8,
                  ),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(20),
                  ),
                ),
                child: const Text("See more"),
              ),
            ],
          ),
          const SizedBox(height: 20),
          GridView.count(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            crossAxisCount: 2,
            childAspectRatio: 4,
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            children: [
              AirConditionItem(
                icon: Icons.thermostat,
                label: 'Real Feel',
                value: "${data["temperature"]["realNow"]} $temperatureSymbol",
              ),
              Padding(
                padding: EdgeInsets.only(left: 25),
                child: AirConditionItem(
                  icon: Icons.air,
                  label: 'Wind',
                  value: "${data["wind"]["spd"]} $windSymbol",
                  trailing: Transform.rotate(
                    angle: _directionToAngle(data["wind"]["dir"]),
                    child: const Icon(
                      Icons.arrow_upward,
                      color: Colors.white,
                      size: 20,
                    ),
                  ),
                ),
              ),
              AirConditionItem(
                icon: Icons.water_drop,
                label: 'Chance of rain',
                value: "${data["precipitation"]["proba"]}%",
              ),
              Padding(
                padding: EdgeInsets.only(left: 30),
                child: AirConditionItem(
                  icon: Icons.wb_sunny,
                  label: 'UV Index',
                  value: data["sun"]["uvi"].toString(),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class AirConditionItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Widget? trailing;

  const AirConditionItem({
    super.key,
    required this.icon,
    required this.label,
    required this.value,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, color: Colors.white),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: const TextStyle(color: Colors.grey, fontSize: 12),
              ),
              Row(
                children: [
                  Text(
                    value,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  if (trailing != null) ...[
                    const SizedBox(width: 2.5),
                    trailing!,
                  ],
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
}

import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';

class ForecastCard extends StatelessWidget {
  final List<Map<String, dynamic>> hourlyForecast;
  final SpriteSheet spriteSheet;

  const ForecastCard({
    super.key,
    required this.hourlyForecast,
    required this.spriteSheet,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      color: const Color(0xFF1B2430),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "Today's Forecast",
              style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 100,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: hourlyForecast.length,
                itemBuilder: (context, index) {
                  final hourData = hourlyForecast[index];
                  final time = hourData["time"];
                  final temp = hourData["temperature"];
                  final iconIndex = hourData["icon"]; // Assuming your icons are indexed in the SpriteSheet

                  return Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 8.0),
                    child: Column(
                      children: [
                        Text(
                          time,
                          style: const TextStyle(color: Colors.white70),
                        ),
                        const SizedBox(height: 8),
                        SizedBox(
                          width: 40,
                          height: 40,
                          // child: spriteSheet.getSpriteById(iconIndex),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          "$temp°C",
                          style: const TextStyle(color: Colors.white),
                        ),
                      ],
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
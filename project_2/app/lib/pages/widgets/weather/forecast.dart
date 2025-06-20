import 'package:app/objects.dart';
import 'package:flame/sprite.dart';
import 'package:flame/widgets.dart';
import 'package:flutter/material.dart';

class WeatherForecast extends StatefulWidget {
  final List<dynamic> hourlyForecast;
  final SpriteSheet spriteSheet;
  final bool isToday;
  final int temperatureMetric;

  const WeatherForecast({
    super.key,
    required this.hourlyForecast,
    required this.spriteSheet,
    required this.isToday,
    required this.temperatureMetric
  });

  @override
  State<WeatherForecast> createState() => _WeatherForecastState();
}

class _WeatherForecastState extends State<WeatherForecast> {
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    // delay to allow layout to build before scrolling
    WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToRelevantHour());
  }

  void _scrollToRelevantHour() {
    final index = widget.isToday 
      ? widget.hourlyForecast.indexWhere((hourData) {
          final hour = int.parse(hourData["datetime"].substring(0, 2));
          final now = DateTime.now().hour;
          return hour >= now;
        })
      : widget.hourlyForecast.indexWhere((hourData) {
          final hour = int.parse(hourData["datetime"].substring(0, 2));
          return hour == 8;
        });

    if (index != -1) {
      const itemWidth = 96.0; // adjust to your card+separator width
      final offset = index * itemWidth;

      Future.microtask(() {
        if (_scrollController.hasClients) {
          _scrollController.jumpTo(offset);
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final String title = widget.isToday ? "Today's Forecast" : "Tomorrow's Forecast";
    final temperatureSymbol = widget.temperatureMetric == 0 ? "º" : "F";

    return Card(
      color: const Color(0xFF1B2430),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: TextStyle(
                  color: Colors.grey, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 140,
              child: ListView.separated(
                controller: _scrollController,
                scrollDirection: Axis.horizontal,
                itemCount: widget.hourlyForecast.length,
                itemBuilder: (context, index) {
                  final hourData = widget.hourlyForecast[index];
                  final time = hourData["datetime"].substring(0, 5);
                  final temp = hourData["temp"];
                  final iconIndex = hourData["icon"];
                  final pos = iconMap[iconIndex] ?? {"row": 0, "column": 0};
                  final sprite =
                      widget.spriteSheet.getSprite(pos["row"]!, pos["column"]!);

                  return SizedBox(
                    width: 80,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          time,
                          style: const TextStyle(
                              color: Colors.grey, fontSize: 14),
                        ),
                        SizedBox(height: 8),
                        SizedBox(
                          width: 60,
                          height: 60,
                          child: SpriteWidget(sprite: sprite),
                        ),
                        Text(
                          "$temp $temperatureSymbol",
                          style: const TextStyle(
                              color: Colors.white,
                              fontSize: 16,
                              fontWeight: FontWeight.w500),
                        ),
                      ],
                    ),
                  );
                },
                separatorBuilder: (context, index) => Container(
                  width: 1,
                  height: 80,
                  color: Colors.white24,
                  margin: const EdgeInsets.symmetric(horizontal: 8),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
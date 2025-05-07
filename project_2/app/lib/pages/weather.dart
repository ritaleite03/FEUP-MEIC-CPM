import 'dart:async';
import 'package:app/logic/requests.dart';
import 'package:flame/flame.dart';
import 'package:flame/components.dart';
import 'package:flame/sprite.dart';
import 'package:flame/widgets.dart';
import 'package:flutter/material.dart';

const Map<String, Map<String, int>> iconMap = {
  "showers-night": {"row": 0, "column": 1},
  "showers-day": {"row": 0, "column": 2},
  "rain": {"row": 1, "column": 2},
  "partly-cloudy-night": {"row": 1, "column": 3},
  "partly-cloudy-day": {"row": 2, "column": 0},
  "fog": {"row": 2, "column": 2},
  "cloudy": {"row": 2, "column": 3},
  "clear-night": {"row": 3, "column": 0},
  "clear-day": {"row": 3, "column": 1},
  "wind": {"row": 3, "column": 2},
  "thunder-showers-night": {"row": 3, "column": 3},
  "thunder-showers-day": {"row": 4, "column": 0},
  "thunder-rain": {"row": 4, "column": 1},
  "snow-showers-night": {"row": 4, "column": 3},
  "snow-showers-day": {"row": 5, "column": 0},
  "snow": {"row": 5, "column": 1},
};

class ButtonsWidget extends StatefulWidget {
  final Map<String, dynamic> data;
  const ButtonsWidget({super.key, required this.data});

  @override
  State<ButtonsWidget> createState() => _ButtonsWidgetState();
}

class _ButtonsWidgetState extends State<ButtonsWidget> {
  String selected = '';

  void handleButtonPress(String label) {
    setState(() => selected = label);
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    Widget buildWeatherButton({
      required IconData icon,
      required String label,
      required String value,
    }) {
      final isSelected = selected == label;
      return Expanded(
        child: AnimatedContainer(
          duration: Duration(milliseconds: 300),
          curve: Curves.easeInOut,
          child: ElevatedButton(
            onPressed: () => handleButtonPress(label),
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
              elevation: isSelected ? 12 : 4,
              backgroundColor:
                  isSelected ? colorScheme.primary : colorScheme.surfaceVariant,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
                side: BorderSide(
                  color: isSelected ? Colors.white : Colors.transparent,
                  width: 2,
                ),
              ),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  icon,
                  size: 28,
                  color: isSelected ? Colors.white : colorScheme.primary,
                ),
                const SizedBox(height: 8),
                Text(
                  label,
                  style: TextStyle(
                    fontWeight: FontWeight.w600,
                    fontSize: 14,
                    color: isSelected ? Colors.white : colorScheme.primary,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  value,
                  style: TextStyle(
                    fontSize: 13,
                    color: isSelected ? Colors.white70 : colorScheme.primary,
                  ),
                ),
              ],
            ),
          ),
        ),
      );
    }

    final data = widget.data;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16.0),
      child: Row(
        children: [
          buildWeatherButton(
            icon: Icons.thermostat,
            label: "Temperature",
            value:
                "${data["temperature"]["realMax"]} F / ${data["temperature"]["realMin"]} F",
          ),
          const SizedBox(width: 12),
          buildWeatherButton(
            icon: Icons.water_drop,
            label: "Precipitation",
            value:
                "${data["precipitation"]["dimen"]} - ${data["precipitation"]["proba"]}%",
          ),
          const SizedBox(width: 12),
          buildWeatherButton(
            icon: Icons.air,
            label: "Wind",
            value: "${data["wind"]["spd"]} km/h - ${data["wind"]["dir"]}",
          ),
        ],
      ),
    );
  }
}

class MainWidget extends StatefulWidget {
  final Map<String, dynamic> data;
  final String icon;
  final SpriteSheet spriteSheet;

  const MainWidget({
    super.key,
    required this.data,
    required this.icon,
    required this.spriteSheet,
  });

  @override
  State<MainWidget> createState() => _MainWidgetState();
}

class _MainWidgetState extends State<MainWidget> with TickerProviderStateMixin {
  late Sprite sprite;
  late SpriteComponent spriteComponent;
  late double scale;

  @override
  void initState() {
    super.initState();
    final pos = iconMap[widget.icon] ?? {"row": 0, "column": 0};
    sprite = widget.spriteSheet.getSprite(pos["row"]!, pos["column"]!);
    spriteComponent = SpriteComponent(sprite: sprite, size: Vector2(100, 100));
    scale = 0.9;
  }

  @override
  Widget build(BuildContext context) {
    final temperature = widget.data["temperature"]["realNow"];
    final backgroundColor = Theme.of(context).colorScheme.surfaceVariant;

    return Container(
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.3),
            blurRadius: 16,
            spreadRadius: 4,
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Transform.scale(
              scale: scale,
              child: Container(
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: Theme.of(
                        context,
                      ).colorScheme.primary.withOpacity(0.3),
                      blurRadius: 10,
                      spreadRadius: 6,
                    ),
                  ],
                ),
                child: SpriteWidget(sprite: spriteComponent.sprite!),
              ),
            ),
            const SizedBox(width: 24),

            Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  "$temperature F",
                  style: TextStyle(
                    fontSize: 50,
                    fontWeight: FontWeight.w900,
                    letterSpacing: -1.5,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
                Text(
                  "(Right Now)",
                  style: TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w500,
                    color: Theme.of(
                      context,
                    ).colorScheme.primary.withOpacity(0.85),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
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
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.primary,
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.primary,
        foregroundColor: Theme.of(context).colorScheme.primaryContainer,
        elevation: 0,
        title: Text(
          widget.cityName ?? '',
          style: TextStyle(
            color: Theme.of(context).colorScheme.onPrimary,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      body: FutureBuilder<Map<String, dynamic>>(
        future: data,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasData) {
            final weather = snapshot.data!;
            final today = weather["today"];
            return FutureBuilder<SpriteSheet>(
              future: spriteSheetFuture,
              builder: (context, spriteSnapshot) {
                if (spriteSnapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                } else if (spriteSnapshot.hasData) {
                  return Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      children: [
                        MainWidget(
                          data: today,
                          icon: today["info"]["icon"],
                          spriteSheet: spriteSnapshot.data!,
                        ),
                        ButtonsWidget(data: today),
                      ],
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

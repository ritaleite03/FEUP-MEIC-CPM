import 'package:app/objects.dart';
import 'package:flame/sprite.dart';
import 'package:flame/widgets.dart';
import 'package:flutter/material.dart';

class WeatherMain extends StatelessWidget {
  final String icon;
  final String temperature;
  final SpriteSheet spriteSheet;

  const WeatherMain({
    super.key,
    required this.icon,
    required this.temperature,
    required this.spriteSheet
  });

  @override
  Widget build(BuildContext context) {
    final pos = iconMap[icon] ?? {"row": 0, "column": 0};
    final sprite = spriteSheet.getSprite(pos["row"]!, pos["column"]!);

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Flexible(
          fit: FlexFit.loose,
          child: Transform.scale(
            scale: 2,
            child: SpriteWidget(sprite: sprite),
          ),
        ),
        Text(
          "$temperatureº",
          style: const TextStyle(
            color: Colors.white,
            fontSize: 60,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}
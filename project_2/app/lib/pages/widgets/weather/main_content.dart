import 'package:app/objects.dart';
import 'package:flame/sprite.dart';
import 'package:flame/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';



class WeatherMain extends StatelessWidget {
  final String icon;
  final String temperature;
  final String temperatureMax;
  final String temperatureMin;
  final SpriteSheet spriteSheet;
  final int temperatureMetric;

  const WeatherMain({
    super.key,
    required this.icon,
    required this.temperature,
    required this.temperatureMax,
    required this.temperatureMin,
    required this.spriteSheet,
    required this.temperatureMetric
  });

  @override
  Widget build(BuildContext context) {
    final pos = iconMap[icon] ?? {"row": 0, "column": 0};
    final sprite = spriteSheet.getSprite(pos["row"]!, pos["column"]!);
    //final img = SvgPicture.asset(
    //    'assets/images/icons/$icon.svg',
    //    width: 100,
    //    height: 100,
    //  );
    final temperatureSymbol = temperatureMetric == 0 ? "º" : "F";

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Flexible(
          fit: FlexFit.loose,
          child: Transform.scale(
            scale: 2,
            child: SpriteWidget(sprite: sprite),
            //child: img,
          ),
        ),
        Text(
          "$temperature$temperatureSymbol",
          style: const TextStyle(
            color: Colors.white,
            fontSize: 60,
            fontWeight: FontWeight.bold,
          ),
        ),
        Text(
          "$temperatureMax$temperatureSymbol / $temperatureMin$temperatureSymbol",
          style: const TextStyle(
            color: Colors.white,
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}
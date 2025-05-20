import 'dart:math';

import 'package:flutter/material.dart';

class AirConditionsDetails extends StatelessWidget {
  final Map<String, dynamic> data;

  const AirConditionsDetails({
    super.key,
    required this.data
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
    final boxDecoration = BoxDecoration(
      color: const Color(0xFF1B2430),
      borderRadius: BorderRadius.circular(12),
    );

    final textStyle = TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold);

    Widget buildBox(String label, String value, Icon icon, {String? windDirection}) {
      return Container(
        decoration: boxDecoration,
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                icon,
                const SizedBox(width: 6),
                Flexible(
                  child: Text(
                    label,
                    style: const TextStyle(
                      color: Colors.grey,
                      fontWeight: FontWeight.bold,
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
            const Spacer(),
            Center(
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    value,
                    style: textStyle,
                  ),
                  if (windDirection != null) ...[
                    const SizedBox(width: 6),
                    Transform.rotate(
                      angle: _directionToAngle(windDirection),
                      child: const Icon(Icons.arrow_upward, size: 16),
                    ),
                  ],
                ],
              ),
            ),
            const Spacer(),
          ],
        ),
      );
    }

    return GridView.count(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisCount: 2,
      mainAxisSpacing: 15,
      crossAxisSpacing: 15,
      childAspectRatio: 1.8,
      children: [
        buildBox("Min-Max Temp", "${data["temperature"]["realMin"].toString()}º - ${data["temperature"]["realMax"].toString()}º", const Icon(Icons.thermostat)),
        buildBox("Min-Max Feel", "${data["temperature"]["feelMin"].toString()}º - ${data["temperature"]["feelMax"].toString()}º", const Icon(Icons.device_thermostat)),
        buildBox("UV Index", data["sun"]["uvi"].toString(), const Icon(Icons.wb_sunny)),
        buildBox("Wind", "${data["wind"]["spd"].toString()} km/h", const Icon(Icons.air), windDirection: data["wind"]["dir"]?.toString()),
        buildBox("Humidity", "${data["humidity"]["hum"].toString()}%", const Icon(Icons.water_drop)),
        buildBox("Pressure", "${data["pressure"]["pres"].toString()}%", const Icon(Icons.speed)),
      ],
    );
  }
}
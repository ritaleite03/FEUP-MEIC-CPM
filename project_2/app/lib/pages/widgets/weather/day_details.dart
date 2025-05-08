import 'dart:math';

import 'package:app/pages/widgets/utils.dart';
import 'package:flutter/material.dart';

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

class DayDetails extends StatefulWidget {
  final Map<String, dynamic> data;
  const DayDetails({super.key, required this.data});

  @override
  State<DayDetails> createState() => _DayDetailsState();
}

class _DayDetailsState extends State<DayDetails> {
  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    Widget buildWeatherButton({
      required IconData icon,
      required String label,
      String? value,
      Widget? valueWidget,
    }) {
      return Expanded(
        child: ContainerWidget(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(icon, size: 28, color: colorScheme.primary),
              const SizedBox(height: 4),
              Text(
                label,
                style: TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 12,
                  color: colorScheme.primary,
                ),
              ),
              const SizedBox(height: 2),
              valueWidget ??
                  Text(
                    value ?? '',
                    style: TextStyle(fontSize: 10, color: colorScheme.primary),
                  ),
            ],
          ),
        ),
      );
    }

    final data = widget.data;

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
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
          valueWidget: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                "${data["wind"]["spd"]} km/h",
                style: TextStyle(fontSize: 10, color: colorScheme.primary),
              ),
              const SizedBox(width: 6),
              Transform.rotate(
                angle: _directionToAngle(data["wind"]["dir"]),
                child: Icon(
                  Icons.arrow_upward,
                  size: 10,
                  color: colorScheme.primary,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

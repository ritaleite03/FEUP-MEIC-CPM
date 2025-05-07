import 'dart:math';

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

class ButtonsWidget extends StatefulWidget {
  final Map<String, dynamic> data;
  const ButtonsWidget({super.key, required this.data});

  @override
  State<ButtonsWidget> createState() => _ButtonsWidgetState();
}

class _ButtonsWidgetState extends State<ButtonsWidget> {
  String selected = 'Temperature';

  void handleButtonPress(String label) {
    setState(() => selected = label);
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    Widget buildWeatherButton({
      required IconData icon,
      required String label,
      String? value,
      Widget? valueWidget,
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
                  isSelected ? colorScheme.primary : colorScheme.surfaceBright,
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
                valueWidget ??
                    Text(
                      value ?? '',
                      style: TextStyle(
                        fontSize: 13,
                        color:
                            isSelected ? Colors.white70 : colorScheme.primary,
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
            valueWidget: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text("${data["wind"]["spd"]} km/h"),
                const SizedBox(width: 6),
                Transform.rotate(
                  angle: _directionToAngle(data["wind"]["dir"]),
                  child: Icon(Icons.arrow_upward, size: 16),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

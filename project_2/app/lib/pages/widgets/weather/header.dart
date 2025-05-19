import 'package:flutter/material.dart';

class WeatherHeader extends StatelessWidget {
  final String city;
  final String rainChance;

  const WeatherHeader({required this.city, required this.rainChance, super.key});

  @override
  Widget build(BuildContext context) {

    return Column(
      children: [
        Text(
          city,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 32,
            fontWeight: FontWeight.bold
          ),
        ),
        Text(
          'Chance of rain: $rainChance%',
          style: const TextStyle(
            color: Colors.white70,
            fontSize: 16
          ),
        )
      ],
    );
  }
}

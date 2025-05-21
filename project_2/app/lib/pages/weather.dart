import 'dart:async';

import 'package:app/logic/metrics.dart';
import 'package:app/pages/favorites.dart';
import 'package:app/pages/settings.dart';
import 'package:app/pages/widgets/weather/conditions.dart';
import 'package:app/pages/widgets/weather/forecast.dart';
import 'package:flame/components.dart';
import 'package:flame/flame.dart';
import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';

import 'package:app/logic/requests.dart';
import 'package:app/pages/widgets/utils.dart';
import 'package:app/pages/widgets/weather/header.dart';
import 'package:app/pages/widgets/weather/main_content.dart';
import 'package:app/pages/week.dart';
import 'package:shared_preferences/shared_preferences.dart';

class WeatherPage extends StatefulWidget {
  final String? cityName;

  const WeatherPage({super.key, this.cityName});

  @override
  State<WeatherPage> createState() => _WeatherPageState();
}

class _WeatherPageState extends State<WeatherPage> {
  late Future<Map<String, dynamic>> data;
  late Future<Map<String, dynamic>> todayTomorrowForecastData;
  late Future<SpriteSheet> spriteSheetFuture;
  String selected = "today";
  int temperatureMetric = 0;
  int windMetric = 0;
  int pressureMetric = 0;

  @override
  void initState() {
    super.initState();
    data = getWeatherNow(widget.cityName ?? 'Unknown');
    todayTomorrowForecastData = getTodayForecast(widget.cityName ?? "Unknown");
    spriteSheetFuture = loadSpriteSheet();
    _saveCurrentCity();
    _loadMetrics();
  }

  void _saveCurrentCity() async {
    final prefs = await SharedPreferences.getInstance();
    prefs.setString('current_city', widget.cityName ?? "");
  }

  _loadMetrics() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      temperatureMetric = prefs.getInt('temperature') ?? 0;
      windMetric = prefs.getInt('wind') ?? 0;
      pressureMetric = prefs.getInt('pressure') ?? 0;
    });
  }

  void _updateMetrics(Map<String, dynamic> object, List<String> targetKeys) {
    for (final key in targetKeys) {
      if (object.containsKey(key)) {
        final nestedMap = object[key] as Map<String, dynamic>;
        object[key] = nestedMap.map((k, v) {
          if (key == "temperature") {
            if (temperatureMetric == 1) {
              return MapEntry(k, celsiusToFahrenheit(v));
            } else {
              if (v is String) {
                return MapEntry(k, double.parse(v).round());
              } else {
                return MapEntry(k, v.round());
              }
            }
          } else if (key == "wind" && k != "dir") {
            if (windMetric == 1) {
              return MapEntry(k, kmhToMps(v));
            } else if (windMetric == 2) {
              return MapEntry(k, kmhToKnots(v));
            } else {
              return MapEntry(k, v);
            }
          } else if (key == "pressure") {
            if (pressureMetric == 1) {
              return MapEntry(k, pressureToInches(v));
            } else if (pressureMetric == 2) {
              return MapEntry(k, pressureToKPA(v));
            } else if (pressureMetric == 3) {
              return MapEntry(k, pressureToMM(v));
            } else {
              return MapEntry(k, v);
            }
          } else {
            return MapEntry(k, v);
          }
        });
      }
    }
  }

  void _updateHourlyTemps(List<dynamic> forecast) {
    for (var i = 0; i < forecast.length; i++) {
      final entry = forecast[i];

      if (entry is Map<String, dynamic>) {
        final temp = entry['temp'];
        if (temperatureMetric == 1) {
          entry['temp'] = celsiusToFahrenheit(temp);
        } else {
          entry['temp'] = temp.round();
        }
      }
    }
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
    final colorScheme = Theme.of(context).colorScheme;

    final metrics = {
      "temperature": temperatureMetric,
      "wind": windMetric,
      "pressure": pressureMetric,
    };

    return Scaffold(
      backgroundColor: colorScheme.primary,
      appBar: AppBarWidget(title: widget.cityName ?? ''),
      body: FutureBuilder<Map<String, dynamic>>(
        future: data,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return _buildLoadingIndicator(colorScheme);
          } else if (snapshot.hasData) {
            final today = snapshot.data!["today"];
            final tomorrow = snapshot.data!["tomorrow"];
            final week = snapshot.data!["week"];
            final selectedData = selected == "today" ? today : tomorrow;

            _updateMetrics(today, ['temperature', 'wind', 'pressure']);
            for (final day in week) {
              _updateMetrics(day, ['temperature', 'wind', 'pressure']);
            }

            return FutureBuilder<SpriteSheet>(
              future: spriteSheetFuture,
              builder: (context, spriteSnapshot) {
                if (spriteSnapshot.connectionState == ConnectionState.waiting) {
                  return _buildLoadingIndicator(colorScheme);
                } else if (spriteSnapshot.hasData) {
                  return FutureBuilder<Map<String, dynamic>>(
                    future: todayTomorrowForecastData,
                    builder: (context, forecastSnapshot) {
                      if (forecastSnapshot.connectionState ==
                          ConnectionState.waiting) {
                        return _buildLoadingIndicator(colorScheme);
                      } else if (forecastSnapshot.hasData) {
                        _updateHourlyTemps(forecastSnapshot.data!["today"]);
                        _updateHourlyTemps(forecastSnapshot.data!["tomorrow"]);

                        return _buildWeatherContent(
                          cityName: widget.cityName!,
                          colorScheme: colorScheme,
                          today: today,
                          tomorrow: tomorrow,
                          week: week,
                          selectedData: selectedData,
                          spriteSheet: spriteSnapshot.data!,
                          todayForecastData: forecastSnapshot.data!["today"],
                          tomorrowForecastData:
                              forecastSnapshot.data!["tomorrow"],
                          metrics: metrics,
                        );
                      } else {
                        return const Center(
                          child: Text("No forecast data available"),
                        );
                      }
                    },
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
      bottomNavigationBar: Theme(
        data: Theme.of(context).copyWith(
          splashFactory: NoSplash.splashFactory,
          highlightColor: Colors.transparent,
        ),
        child: BottomNavigationBar(
          backgroundColor: const Color(0xFF1B2430),
          selectedItemColor: Colors.white,
          unselectedItemColor: Colors.white70,
          currentIndex: 1,
          onTap: (int index) {
            if (index == 0) {
              Navigator.pushReplacement(
                context,
                MaterialPageRoute(builder: (context) => const FavoritesPage()),
              );
            } else if (index == 2) {
              Navigator.pushReplacement(
                context,
                MaterialPageRoute(builder: (context) => const SettingsPage()),
              );
            }
          },
          items: const [
            BottomNavigationBarItem(icon: Icon(Icons.star), label: 'Favorites'),
            BottomNavigationBarItem(
              icon: Icon(Icons.wb_sunny),
              label: 'Weather',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.settings),
              label: 'Settings',
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLoadingIndicator(ColorScheme colorScheme) {
    return Center(
      child: CircularProgressIndicator(color: colorScheme.primaryContainer),
    );
  }

  Widget _buildWeatherContent({
    required String cityName,
    required ColorScheme colorScheme,
    required Map<String, dynamic> today,
    required Map<String, dynamic> tomorrow,
    required List<dynamic> week,
    required Map<String, dynamic> selectedData,
    required SpriteSheet spriteSheet,
    required List<dynamic> todayForecastData,
    required List<dynamic> tomorrowForecastData,
    required Map<String, int> metrics,
  }) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: SingleChildScrollView(
        child: Center(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              WeatherHeader(
                city: cityName,
                rainChance: selectedData["precipitation"]["proba"].toString(),
              ),
              SizedBox(height: 75),
              WeatherMain(
                icon: selectedData["info"]["icon"],
                temperature: selectedData["temperature"]["realNow"].toString(),
                spriteSheet: spriteSheet,
                temperatureMetric: temperatureMetric,
              ),
              SizedBox(height: 20),
              WeatherForecast(
                hourlyForecast: todayForecastData,
                spriteSheet: spriteSheet,
                isToday: true,
                temperatureMetric: temperatureMetric,
              ),
              SizedBox(height: 20),
              WeatherForecast(
                hourlyForecast: tomorrowForecastData,
                spriteSheet: spriteSheet,
                isToday: false,
                temperatureMetric: temperatureMetric,
              ),
              SizedBox(height: 20),
              WeatherConditions(
                cityName: widget.cityName,
                data: today,
                spriteSheet: spriteSheet,
                metrics: metrics,
              ),
              SizedBox(height: 20),
              SizedBox(
                width: double.infinity, // ocupa toda a largura possível
                child: ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder:
                            (context) => WeekPage(
                              cityName: widget.cityName,
                              today: today,
                              week: week,
                              metrics: metrics,
                            ),
                      ),
                    );
                  },
                  child: const Text("See past week weather..."),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

import 'package:app/pages/favorites.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  final List<bool> _tempSelection = [true, false];
  final List<bool> _windSelection = [true, false, false];
  final List<bool> _pressureSelection = [false, false, false, true];
  final List<bool> _precipSelection = [true, false];
  final List<bool> _distanceSelection = [true, false];

  void _saveSelection(String key, int index) async {
    final prefs = await SharedPreferences.getInstance();
    prefs.setInt(key, index);
  }

  void _loadSelection(String key, List<bool> selection) async {
    final prefs = await SharedPreferences.getInstance();
    int index = prefs.getInt(key) ?? 0;
    print(key);
    print(index);
    setState(() {
      for (int i = 0; i < selection.length; i++) {
        selection[i] = i == index;
      }
    });
  }

  @override
  void initState() {
    super.initState();
    _loadSelection('temperature', _tempSelection);
    _loadSelection('wind', _windSelection);
    _loadSelection('pressure', _pressureSelection);
    _loadSelection('precip', _precipSelection);
    _loadSelection('distance', _distanceSelection);
  }

  Widget _buildSubSection(String title, List<String> labels, List<bool> selections, Function(int) onPressed) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              color: Colors.white70,
              fontWeight: FontWeight.bold,
              fontSize: 14,
            ),
          ),
          const SizedBox(height: 12),
          Row(
            children: List.generate(labels.length, (index) {
              return Expanded(
                child: OutlinedButton(
                  style: OutlinedButton.styleFrom(
                    backgroundColor: selections[index] ? const Color(0xFF2D3A4C) : Colors.transparent,
                    foregroundColor: selections[index] ? Colors.white : Colors.white70,
                    side: BorderSide(
                      color: selections[index] ? const Color(0xFF2D3A4C) : Colors.white24,
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.horizontal(
                        left: index == 0 ? const Radius.circular(12) : Radius.zero,
                        right: index == labels.length - 1 ? const Radius.circular(12) : Radius.zero,
                      ),
                    ),
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    splashFactory: NoSplash.splashFactory,
                  ),
                  onPressed: () {
                    onPressed(index);
                  },
                  child: Text(labels[index]),
                ),
              );
            }),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0C141F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0C141F),
        title: const Text('Settings'),
        elevation: 0,
        automaticallyImplyLeading: false
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Padding(
              padding: EdgeInsets.only(left: 8.0),
              child: Text(
                'Units',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 30,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF1B2430),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildSubSection(
                    'TEMPERATURE',
                    ['Celsius', 'Fahrenheit'],
                    _tempSelection,
                    (index) {
                      setState(() {
                        for (int i = 0; i < _tempSelection.length; i++) {
                          _tempSelection[i] = i == index;
                        }
                      });
                      _saveSelection('temperature', index);
                    },
                  ),
                  _buildSubSection(
                    'WIND SPEED',
                    ['km/h', 'm/s', 'Knots'],
                    _windSelection,
                    (index) {
                      setState(() {
                        for (int i = 0; i < _windSelection.length; i++) {
                          _windSelection[i] = i == index;
                        }
                      });
                      _saveSelection('wind', index);
                    },
                  ),
                  _buildSubSection(
                    'PRESSURE',
                    ['hPa', 'Inches', 'kPa', 'mm'],
                    _pressureSelection,
                    (index) {
                      setState(() {
                        for (int i = 0; i < _pressureSelection.length; i++) {
                          _pressureSelection[i] = i == index;
                        }
                      });
                      _saveSelection('pressure', index);
                    },
                  ),
                  _buildSubSection(
                    'PRECIPITATION',
                    ['Millimeters', 'Inches'],
                    _precipSelection,
                    (index) {
                      setState(() {
                        for (int i = 0; i < _precipSelection.length; i++) {
                          _precipSelection[i] = i == index;
                        }
                      });
                      _saveSelection('precip', index);
                    },
                  ),
                  _buildSubSection(
                    'DISTANCE',
                    ['Kilometers', 'Miles'],
                    _distanceSelection,
                    (index) {
                      setState(() {
                        for (int i = 0; i < _distanceSelection.length; i++) {
                          _distanceSelection[i] = i == index;
                        }
                      });
                      _saveSelection('distance', index);
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
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
            }
          },
          items: const [
            BottomNavigationBarItem(
              icon: Icon(Icons.star),
              label: 'Favorites',
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
}

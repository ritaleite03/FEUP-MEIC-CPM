class City {
  String name;
  String path;
  int isFavorite;

  City({required this.name, required this.path, this.isFavorite = 0});

  Map<String, Object?> toMap() {
    return {'name': name, 'path': path, 'isFavorite': isFavorite};
  }

  @override
  String toString() {
    return 'City{name: $name, path: $path, isFavorite: $isFavorite}';
  }
}

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

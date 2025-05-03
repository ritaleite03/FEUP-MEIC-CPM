class City {
  String name;
  int isFavorite;

  City({required this.name, this.isFavorite = 0});

  Map<String, Object?> toMap() {
    return {'name': name, 'isFavorite': isFavorite};
  }

  @override
  String toString() {
    return 'City{name: $name, isFavorite: $isFavorite}';
  }
}

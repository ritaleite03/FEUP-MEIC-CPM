import 'package:app/objects.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

late final Future<Database> database;

Future<void> initDatabase() async {
  final dbPath = await getDatabasesPath();
  final db = await openDatabase(
    join(dbPath, 'weather.db'),
    onCreate: (db, version) async {
      await db.execute(
        'CREATE TABLE city(name TEXT PRIMARY KEY, isFavorite INTEGER)',
      );

      final List<City> initialCities = [
        City(name: 'Aveiro'),
        City(name: 'Beja'),
        City(name: 'Braga'),
        City(name: 'Braganca'),
        City(name: 'Castelo Branco'),
        City(name: 'Coimbra'),
        City(name: 'Évora'),
        City(name: 'Faro'),
        City(name: 'Guarda'),
        City(name: 'Leiria'),
        City(name: 'Lisbon'),
        City(name: 'Portalegre'),
        City(name: 'Porto'),
        City(name: 'Santarem'),
        City(name: 'Setubal'),
        City(name: 'Viana do Castelo'),
        City(name: 'Vila Real'),
        City(name: 'Viseu'),
        City(name: 'Funchal'),
        City(name: 'Ponta Delgada'),
      ];

      for (var city in initialCities) {
        await db.insert(
          'city',
          city.toMap(),
          conflictAlgorithm: ConflictAlgorithm.replace,
        );
      }
    },
    version: 1,
  );

  database = Future.value(db);
}

Future<void> insertCity(City city) async {
  final db = await database;
  await db.insert(
    'city',
    city.toMap(),
    conflictAlgorithm: ConflictAlgorithm.replace,
  );
}

Future<void> updateCity(City city) async {
  final db = await database;
  await db.update(
    'city',
    city.toMap(),
    where: 'name = ?',
    whereArgs: [city.name],
  );
}

Future<List<City>> getCities() async {
  final db = await database;
  final List<Map<String, Object?>> cities = await db.query('city');
  return [
    for (final {'name': name as String, 'isFavorite': isFavorite as int}
        in cities)
      City(name: name, isFavorite: isFavorite),
  ];
}

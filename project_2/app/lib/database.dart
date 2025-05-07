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
        'CREATE TABLE city(name TEXT PRIMARY KEY, path TEXT, isFavorite INTEGER)',
      );

      final List<City> initialCities = [
        City(name: 'Aveiro', path: 'assets/city/aveiro.jpg'),
        City(name: 'Beja', path: 'assets/city/beja.jpg'),
        City(name: 'Braga', path: 'assets/city/braga.jpg'),
        City(name: 'Braganca', path: 'assets/city/braganca.jpg'),
        City(name: 'Castelo Branco', path: 'assets/city/castelo_branco.jpg'),
        City(name: 'Coimbra', path: 'assets/city/coimbra.jpg'),
        City(name: 'Evora', path: 'assets/city/evora.jpg'),
        City(name: 'Faro', path: 'assets/city/faro.jpg'),
        City(name: 'Guarda', path: 'assets/city/guarda.jpg'),
        City(name: 'Leiria', path: 'assets/city/leiria.jpg'),
        City(name: 'Lisbon', path: 'assets/city/lisbon.jpg'),
        City(name: 'Portalegre', path: 'assets/city/portalegre.jpg'),
        City(name: 'Porto', path: 'assets/city/porto.jpg'),
        City(name: 'Santarem', path: 'assets/city/santarem.jpg'),
        City(name: 'Setubal', path: 'assets/city/setubal.jpg'),
        City(
          name: 'Viana do Castelo',
          path: 'assets/city/viana_do_castelo.jpg',
        ),
        City(name: 'Vila Real', path: 'assets/city/vila_real.jpg'),
        City(name: 'Viseu', path: 'assets/city/viseu.jpg'),
        City(name: 'Funchal', path: 'assets/city/funchal.jpg'),
        City(name: 'Ponta Delgada', path: 'assets/city/ponta_delgada.jpg'),
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
    for (final {
          'name': name as String,
          'path': path as String,
          'isFavorite': isFavorite as int,
        }
        in cities)
      City(name: name, path: path, isFavorite: isFavorite),
  ];
}

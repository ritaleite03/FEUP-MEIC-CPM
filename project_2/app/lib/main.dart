import 'package:app/database.dart';
import 'package:app/pages/favorites.dart';
import 'package:app/pages/settings.dart';
import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

// import 'logic/requests.dart';

Future<void> main() async {
  await dotenv.load(fileName: ".env");  // Load the .env file
  WidgetsFlutterBinding.ensureInitialized();
  initDatabase();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.dark(
          primary: Color(0xFF0C141F),
          surface: Color(0xFF384560),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFF2196F3), // consistent button color
            foregroundColor: Colors.white, // text/icon color
          ),
        ),
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.primary,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Image.asset('assets/images/weather.png', width: 200, height: 200),
            const Text(
              'Weather',
              style: TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.w900,
                fontSize: 50,
              ),
            ),
            const Text(
              'ForeCasts',
              style: TextStyle(
                color: Color(0xFF2196F3),
                fontWeight: FontWeight.normal,
                fontSize: 30,
              ),
            ),
            const SizedBox(height: 50),
            ElevatedButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const FavoritesPage(),
                  ),
                );
              },
              style: ButtonStyle(
                backgroundColor: WidgetStateProperty.all(const Color(0xFF2196F3)),
                foregroundColor: WidgetStateProperty.all(
                  Colors.white
                ),
              ),
              child: Text("Get Start"),
            ),
          ],
        ),
      ),
    );
  }
}

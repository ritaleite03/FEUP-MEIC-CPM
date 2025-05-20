import 'package:app/database.dart';
import 'package:app/objects.dart';
import 'package:app/pages/settings.dart';
import 'package:app/pages/weather.dart';
import 'package:app/pages/widgets/utils.dart';
import 'package:flutter/material.dart';

Icon favoritesIcon(City city) {
  return city.isFavorite == 1
      ? const Icon(Icons.favorite, color: Colors.red)
      : const Icon(Icons.favorite_border_outlined);
}

class _FavoritesPageSearchBar extends StatelessWidget {
  final List<City> cities;
  final void Function(City) onToggleFavorite;

  const _FavoritesPageSearchBar({
    required this.cities,
    required this.onToggleFavorite,
  });

  @override
  Widget build(BuildContext context) {
    return SearchAnchor(
      builder: (BuildContext context, SearchController controller) {
        return SearchBar(
          controller: controller,
          hintText: 'Search for new locations...',
          padding: const WidgetStatePropertyAll<EdgeInsets>(
            EdgeInsets.symmetric(horizontal: 16.0),
          ),
          onTap: controller.openView,
          onChanged: (_) => controller.openView(),
          leading: const Icon(Icons.search),
        );
      },
      suggestionsBuilder: (BuildContext context, SearchController controller) {
        final query = controller.text.toLowerCase();
        final filteredCities =
            cities
                .where((city) => city.name.toLowerCase().contains(query))
                .toList();

        return filteredCities.map((city) {
          return ListTile(
            title: Text(city.name),
            trailing: IconButton(
              onPressed: () {
                onToggleFavorite(city);
                controller.closeView(city.name);
              },
              icon: favoritesIcon(city),
            ),
          );
        }).toList();
      },
    );
  }
}

class FavoritesPage extends StatefulWidget {
  const FavoritesPage({super.key});

  @override
  State<FavoritesPage> createState() => _FavoritesPageState();
}

class _FavoritesPageState extends State<FavoritesPage> {
  List<City> cities = [];
  List<City> citiesFav = [];

  void onToggleFavorite(City city) {
    setState(() {
      city.isFavorite = city.isFavorite == 1 ? 0 : 1;
      citiesFav = cities.where((c) => c.isFavorite == 1).toList();
      updateCity(city);
    });
  }

  @override
  void initState() {
    super.initState();
    loadCities();
  }

  Future<void> loadCities() async {
    final loadedCities = await getCities();
    setState(() {
      cities = loadedCities;
      citiesFav = loadedCities.where((city) => city.isFavorite == 1).toList();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.primary,
      appBar: AppBarWidget(),
      body: Padding(
        padding: const EdgeInsets.fromLTRB(8.0, 8.0, 8.0, 8.0),
        child: Column(
          children: [
            _FavoritesPageSearchBar(
              cities: cities,
              onToggleFavorite: onToggleFavorite,
            ),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: ListView.builder(
                  itemCount: citiesFav.length,
                  itemBuilder: (context, index) {
                    final item = citiesFav[index];
                    return Padding(
                      padding: const EdgeInsets.fromLTRB(0, 4.0, 0, 0),
                      child: Card(
                        child: Column(
                          children: [
                            Image.asset(
                              item.path,
                              height: 120,
                              width: double.infinity,
                              fit: BoxFit.cover,
                            ),
                            Padding(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 8.0,
                                vertical: 4.0,
                              ),
                              child: Row(
                                mainAxisAlignment:
                                    MainAxisAlignment.spaceBetween,
                                children: [
                                  Row(
                                    children: [
                                      IconButton(
                                        onPressed: () {
                                          onToggleFavorite(item);
                                        },
                                        icon: favoritesIcon(item),
                                      ),
                                      Text(item.name),
                                    ],
                                  ),
                                  IconButton(
                                    icon: Icon(
                                      Icons.arrow_forward_ios,
                                      color:
                                          Theme.of(context).colorScheme.surface,
                                    ),
                                    onPressed: () {
                                      Navigator.push(
                                        context,
                                        MaterialPageRoute(
                                          builder:
                                              (context) => WeatherPage(
                                                cityName: item.name,
                                              ),
                                        ),
                                      );
                                    },
                                    style: ButtonStyle(
                                      backgroundColor: WidgetStateProperty.all(
                                        const Color(0xFF2196F3),
                                      ),
                                      shape: WidgetStateProperty.all(
                                        const CircleBorder(),
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: Theme(
        data: Theme.of(context).copyWith(
          splashFactory: NoSplash.splashFactory,
        ),
        child: BottomNavigationBar(
          backgroundColor: const Color(0xFF1B2430),
          selectedItemColor: Colors.white,
          unselectedItemColor: Colors.white70,
          currentIndex: 0,
          onTap: (int index) {
            if (index == 1) {
              Navigator.pushReplacement(
                context,
                MaterialPageRoute(builder: (context) => const SettingsPage()),
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

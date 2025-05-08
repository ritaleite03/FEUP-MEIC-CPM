import 'package:app/objects.dart';
import 'package:app/pages/widgets/utils.dart';
import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';
import 'package:flame/widgets.dart';

class DayMain extends StatelessWidget {
  final Map<String, dynamic> data;
  final String icon;
  final SpriteSheet spriteSheet;

  const DayMain({
    super.key,
    required this.data,
    required this.icon,
    required this.spriteSheet,
  });

  @override
  Widget build(BuildContext context) {
    final pos = iconMap[icon] ?? {"row": 0, "column": 0};
    final sprite = spriteSheet.getSprite(pos["row"]!, pos["column"]!);
    final temperature = data["temperature"]["realNow"];
    final description = data["info"]["desc"];

    final titleStyle = TextStyle(
      fontWeight: FontWeight.bold,
      fontSize: 14,
      // ignore: deprecated_member_use
      color: Theme.of(context).colorScheme.primary.withOpacity(0.9),
    );

    return IntrinsicHeight(
      child: Row(
        children: [
          Expanded(
            child: ContainerWidget(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Padding(
                    padding: const EdgeInsets.only(bottom: 6.0, top: 4),
                    child: Text(
                      "Status",
                      textAlign: TextAlign.center,
                      style: titleStyle,
                    ),
                  ),
                  const Divider(height: 1),
                  Expanded(
                    child: Center(
                      child: Transform.scale(
                        scale: 1.2,
                        child: SpriteWidget(sprite: sprite),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              children: [
                ContainerWidget(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Padding(
                        padding: const EdgeInsets.only(bottom: 6.0, top: 4),
                        child: Text(
                          "Temperature",
                          textAlign: TextAlign.center,
                          style: titleStyle,
                        ),
                      ),
                      const Divider(height: 1),
                      Padding(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        child: Text(
                          "$temperature F",
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 30,
                            fontWeight: FontWeight.w900,
                            letterSpacing: -2,
                            color: Theme.of(context).colorScheme.primary,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 12),
                ContainerWidget(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Padding(
                        padding: const EdgeInsets.only(bottom: 6.0, top: 4),
                        child: Text(
                          "Description",
                          textAlign: TextAlign.center,
                          style: titleStyle,
                        ),
                      ),
                      const Divider(height: 1),
                      SingleChildScrollView(
                        child: Padding(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          child: Text(
                            "$description",
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontSize: 15,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

import 'package:app/objects.dart';
import 'package:flame/sprite.dart';
import 'package:flutter/material.dart';
import 'package:flame/widgets.dart';

class DayWidget extends StatelessWidget {
  final Map<String, dynamic> data;
  final String icon;
  final SpriteSheet spriteSheet;

  const DayWidget({
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

    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceBright,
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            // ignore: deprecated_member_use
            color: Colors.black.withOpacity(0.3),
            blurRadius: 16,
            spreadRadius: 4,
          ),
        ],
      ),
      padding: const EdgeInsets.all(16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Transform.scale(
            scale: 0.9,
            child: Container(
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                boxShadow: [
                  BoxShadow(
                    color: Theme.of(
                      context,
                      // ignore: deprecated_member_use
                    ).colorScheme.primary.withOpacity(0.3),
                    blurRadius: 10,
                    spreadRadius: 6,
                  ),
                ],
              ),
              child: SpriteWidget(sprite: sprite),
            ),
          ),
          const SizedBox(width: 24),
          Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Text(
                "$temperature F",
                style: TextStyle(
                  fontSize: 50,
                  fontWeight: FontWeight.w900,
                  letterSpacing: -1.5,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
              Text(
                "(Right Now)",
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w500,
                  color: Theme.of(
                    context,
                    // ignore: deprecated_member_use
                  ).colorScheme.primary.withOpacity(0.85),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

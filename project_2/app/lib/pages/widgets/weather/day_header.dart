import 'package:app/pages/widgets/utils.dart';
import 'package:flutter/material.dart';

class DayHeader extends StatelessWidget {
  final String selected;
  final void Function() onToggle;

  const DayHeader({required this.selected, required this.onToggle, super.key});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return ContainerWidget(
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Align(
            alignment: Alignment.centerLeft,
            child:
                selected == "today"
                    ? const Text("")
                    : IconButton(
                      icon: Icon(
                        Icons.arrow_back_ios,
                        color: colorScheme.primary,
                      ),
                      onPressed: onToggle,
                      style: ButtonStyle(
                        backgroundColor: WidgetStateProperty.all(Colors.amber),
                        shape: WidgetStateProperty.all(CircleBorder()),
                      ),
                    ),
          ),
          Align(
            alignment: Alignment.center,
            child: Text(
              selected.toUpperCase(),
              style: TextStyle(color: colorScheme.primary, fontSize: 20),
            ),
          ),
          Align(
            alignment: Alignment.centerRight,
            child:
                selected != "today"
                    ? const Text("")
                    : IconButton(
                      icon: Icon(
                        Icons.arrow_forward_ios,
                        color: colorScheme.primary,
                      ),
                      onPressed: onToggle,
                      style: ButtonStyle(
                        backgroundColor: WidgetStateProperty.all(Colors.amber),
                        shape: WidgetStateProperty.all(CircleBorder()),
                      ),
                    ),
          ),
        ],
      ),
    );
  }
}

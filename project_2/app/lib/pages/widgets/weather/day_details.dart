import 'package:app/pages/widgets/utils.dart';
import 'package:flutter/material.dart';

class DayDetails extends StatefulWidget {
  final Map<String, dynamic> data;
  final List<String> labels;
  final List<IconData> icons;
  final List<Widget> widgets;
  const DayDetails({
    super.key,
    required this.data,
    required this.labels,
    required this.icons,
    required this.widgets,
  });

  @override
  State<DayDetails> createState() => _DayDetailsState();
}

class _DayDetailsState extends State<DayDetails> {
  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    Widget buildWeatherButton({
      required IconData icon,
      required String label,
      required Widget valueWidget,
    }) {
      return Expanded(
        child: ContainerWidget(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(icon, size: 28, color: colorScheme.primary),
              const SizedBox(height: 4),
              Text(
                label,
                style: TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 12,
                  color: colorScheme.primary,
                ),
              ),
              const SizedBox(height: 2),
              valueWidget,
            ],
          ),
        ),
      );
    }

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        buildWeatherButton(
          icon: widget.icons[0],
          label: widget.labels[0],
          valueWidget: widget.widgets[0],
        ),
        const SizedBox(width: 12),
        buildWeatherButton(
          icon: widget.icons[1],
          label: widget.labels[1],
          valueWidget: widget.widgets[1],
        ),
        const SizedBox(width: 12),
        buildWeatherButton(
          icon: widget.icons[2],
          label: widget.labels[2],
          valueWidget: widget.widgets[2],
        ),
      ],
    );
  }
}

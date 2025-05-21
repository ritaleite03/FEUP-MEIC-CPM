import 'package:app/pages/widgets/utils.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:flutter_colorpicker/flutter_colorpicker.dart';

class WeekPage extends StatefulWidget {
  final String? cityName;
  final dynamic today;
  final dynamic week;
  final Map<String, int> metrics;

  const WeekPage({
    super.key,
    required this.cityName,
    required this.today,
    required this.week,
    required this.metrics
  });

  @override
  State<WeekPage> createState() => _WeekPageState();
}

class _WeekPageState extends State<WeekPage> {
  final List<String> weekDays = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
  late final List<String> weekday = _generateWeekdays();

  Color maxTempColor = Colors.blue;
  Color minTempColor = Colors.greenAccent;

  List<String> _generateWeekdays() {
    final today = DateFormat('EEE', 'en_US').format(DateTime.now());
    final startIndex = weekDays.indexOf(today);
    return List.generate(8, (index) {
      if (index == 7) return "Now";
      return weekDays[(startIndex + index) % 7];
    });
  }

  double parseToDouble(dynamic value) {
    if (value is int) {
      return value.toDouble();
    } else if (value is double) {
      return value;
    } else if (value is String) {
      return double.parse(value);
    }
    throw Exception('Not a numeric value: $value - ${value.runtimeType}');
  }

  Widget _buildLegendItem({
    required Color color,
    required String label,
    required VoidCallback onTap,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 4),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 16,
                height: 16,
                decoration: BoxDecoration(
                  color: color,
                  shape: BoxShape.circle,
                ),
              ),
              const SizedBox(width: 8),
              Text(
                label,
                style: const TextStyle(fontSize: 13, color: Colors.white),
              ),
            ],
          ),
        ),
      ),
    );
  }


  void _showColorPickerDialog(Color currentColor, ValueChanged<Color> onColorSelected, String title) {
    Color selectedColor = currentColor;

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          backgroundColor: Theme.of(context).colorScheme.primaryContainer,
          title: Text('Select color for $title'),
          content: SingleChildScrollView(
            child: ColorPicker(
              pickerColor: selectedColor,
              onColorChanged: (color) {
                selectedColor = color;
              },
              enableAlpha: false,
              showLabel: true,
              pickerAreaHeightPercent: 0.7,
            ),
          ),
          actions: [
            TextButton(
              child: const Text('Cancel'),
              onPressed: () => Navigator.of(context).pop(),
            ),
            TextButton(
              child: const Text('OK'),
              onPressed: () {
                setState(() {
                  onColorSelected(selectedColor);
                });
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Widget buildChartSection({
    required BuildContext context,
    required String title,
    required LineChartData chartData,
    List<Widget>? legends,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        ContainerWidget(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Padding(
                  padding: const EdgeInsets.only(bottom: 12.0, top: 6.0),
                  child: Center(
                    child: Text(
                      title,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
                SizedBox(
                  height: 200,
                  child: LineChart(chartData),
                ),
              ],
            ),
          ),
        ),
        if (legends != null) ...[
          const SizedBox(height: 10),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: Row(
              children: legends,
            ),
          ),
        ],
        const SizedBox(height: 16),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final week = widget.week;
    final today = widget.today;
    final temperatureSymbol = widget.metrics["temperature"] == 0 ? "º" : "F";
    final windSymbol = widget.metrics["wind"] == 0 ? "km/h" : (widget.metrics["wind"] == 1 ? "m/s" : "Knots");

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.primary,
      appBar: AppBarWidget(title: widget.cityName ?? ''),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              buildChartSection(
                context: context,
                title: "Temperature of the last 7 days ($temperatureSymbol)",
                chartData: _buildChartDataTemperature(context, week, today, "temperature"),
                legends: [
                  _buildLegendItem(
                    color: maxTempColor,
                    label: "Max Temperature",
                    onTap: () => _showColorPickerDialog(maxTempColor, (newColor) => maxTempColor = newColor, "Max Temperature"),
                  ),
                  const SizedBox(width: 16),
                  _buildLegendItem(
                    color: minTempColor,
                    label: "Min Temperature",
                    onTap: () => _showColorPickerDialog(minTempColor, (newColor) => minTempColor = newColor, "Min Temperature"),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              buildChartSection(
                context: context,
                title: "Wind Velocity of the last 7 days ($windSymbol)",
                chartData: _buildChartDataTemperature(context, week, today, "wind"),
                legends: [
                  _buildLegendItem(
                    color: maxTempColor,
                    label: "Wind velocity",
                    onTap: () => _showColorPickerDialog(maxTempColor, (newColor) => maxTempColor = newColor, "Wind velocity"),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              buildChartSection(
                context: context,
                title: "Probability of rain of the last 7 days (%)",
                chartData: _buildChartDataTemperature(context, week, today, "precipitation"),
                legends: [
                  _buildLegendItem(
                    color: maxTempColor,
                    label: "Probability of rain",
                    onTap: () => _showColorPickerDialog(maxTempColor, (newColor) => maxTempColor = newColor, "Probability of rain"),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  LineChartData _buildChartDataTemperature(BuildContext context, dynamic week, dynamic today, String metricToDisplay) {
    final colorScheme = Theme.of(context).colorScheme;
    String maxMetric;
    String minMetric;

    if (metricToDisplay == "temperature") {
      maxMetric = "realMax";
      minMetric = "realMin";
    }
    else if (metricToDisplay == "precipitation") {
      maxMetric = minMetric = "proba";
    }
    else {
      maxMetric = minMetric = "spd";
    }

    final allMaxValues = [
      for (var i = 0; i < 7; i++) parseToDouble(week[i][metricToDisplay][maxMetric]),
      parseToDouble(today[metricToDisplay][maxMetric])
    ];

    final allMinValues = [
      for (var i = 0; i < 7; i++) parseToDouble(week[i][metricToDisplay][minMetric]),
      parseToDouble(today[metricToDisplay][minMetric])
    ];

    // calculate minY and maxY with padding
    final maxYValue = allMaxValues.reduce((a, b) => a > b ? a : b) + 5;
    final minYValue = allMinValues.reduce((a, b) => a < b ? a : b) - 5;

    final roundedMaxY = (maxYValue / 5).ceil() * 5 + 5;
    final roundedMinY =  minYValue < 0 ? 0 : (minYValue / 5).floor() * 5 - 5;

    return LineChartData(
      minX: 0,
      maxX: 7,
      minY: roundedMinY.toDouble(),
      maxY: roundedMaxY.toDouble(),
      gridData: FlGridData(
        show: true,
        drawVerticalLine: true,
        drawHorizontalLine: true,
      ),
      titlesData: FlTitlesData(
        topTitles: AxisTitles(
          sideTitles: SideTitles(showTitles: false),
        ),
        rightTitles: AxisTitles(
          sideTitles: SideTitles(showTitles: false),
        ),
        bottomTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            reservedSize: 28,
            getTitlesWidget: (val, meta) => _buildBottom(val, context),
          ),
        ),
        leftTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            reservedSize: 50, // leave enough space for numbers
            interval: 5,
            getTitlesWidget: (value, meta) {
              return Padding(
                padding: const EdgeInsets.only(right: 30, left: 0),
                child: Text(
                  value.toInt().toString(),
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 11,
                  ),
                  textAlign: TextAlign.right,
                ),
              );
            },
          ),
        ),
      ),
      borderData: FlBorderData(
        show: true,
        border: Border.all(color: colorScheme.surfaceBright.withOpacity(0.2)),
      ),
      lineBarsData: _buildLineBars(context, week, today, metricToDisplay, maxMetric, minMetric),
      lineTouchData: LineTouchData(
        touchTooltipData: LineTouchTooltipData(
          tooltipPadding: const EdgeInsets.all(10),
          getTooltipItems: (touchedSpots) => touchedSpots.map((spot) {
            return LineTooltipItem(
              '${spot.y.toStringAsFixed(1)}°',
              TextStyle(
                color: colorScheme.onSurface,
                fontWeight: FontWeight.bold,
              ),
            );
          }).toList(),
        ),
      ),
    );
  }

  Widget _buildBottom(double value, BuildContext context) {
    int index = value.toInt();
    if (index < 0 || index >= weekday.length) return const SizedBox.shrink();
    return Text(
      weekday[index],
      style: Theme.of(context).textTheme.bodyMedium?.copyWith(fontSize: 10),
    );
  }

  List<LineChartBarData> _buildLineBars(BuildContext context, dynamic week, dynamic today, String metricToDisplay, String maxMetric, String minMetric) {

    if (maxMetric != minMetric) {
      return [
        LineChartBarData(
          color: maxTempColor,
          dotData: FlDotData(),
          spots: [
            for (var i = 0; i < 7; i++) FlSpot(i.toDouble(), parseToDouble(week[i][metricToDisplay][maxMetric])),
            FlSpot(7, parseToDouble(today[metricToDisplay][maxMetric])),
          ],
        ),
        LineChartBarData(
          color: minTempColor,
          dotData: FlDotData(),
          spots: [
            for (var i = 0; i < 7; i++) FlSpot(i.toDouble(), parseToDouble(week[i][metricToDisplay][minMetric])),
            FlSpot(7, parseToDouble(today[metricToDisplay][minMetric])),
          ],
        ),
      ];
    }
    else {
      return [
        LineChartBarData(
          color: maxTempColor,
          dotData: FlDotData(),
          spots: [
            for (var i = 0; i < 7; i++) FlSpot(i.toDouble(), parseToDouble(week[i][metricToDisplay][maxMetric])),
            FlSpot(7, parseToDouble(today[metricToDisplay][maxMetric])),
          ],
        ),
      ];
    }
  }
}
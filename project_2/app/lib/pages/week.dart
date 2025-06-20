import 'package:app/pages/widgets/utils.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

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

  Color blue = Colors.blue;
  Color yellow = Colors.yellow;
  Color red = Colors.red;

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
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 4),
      child: InkWell(
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

  Widget buildChartSection({
    required BuildContext context,
    required String title,
    required LineChartData chartData,
    List<Widget>? legends,
    double height = 200,
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
                  height: height,
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
            child: Wrap(
              spacing: 0,
              runSpacing: 0,
              children: legends,
            )
          ),
        ],
        const SizedBox(height: 16),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final week = widget.week;
    final today = widget.today;
    final temperatureSymbol = widget.metrics["temperature"] == 0 ? "º" : "F";
    final windSymbol = widget.metrics["wind"] == 0 ? "km/h" : (widget.metrics["wind"] == 1 ? "m/s" : "Knots");

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.primary,
      appBar: AppBar(
        title: Text("${widget.cityName}'s last week weather"),
        backgroundColor: colorScheme.primary,
        foregroundColor: Colors.white,
      ),
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
                    color: red,
                    label: "Max Temperature"
                  ),
                  _buildLegendItem(
                    color: yellow,
                    label: "Temperature"
                  ),
                  const SizedBox(width: 16),
                  _buildLegendItem(
                    color: blue,
                    label: "Min Temperature"
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
                    color: blue,
                    label: "Wind velocity"
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
                    color: blue,
                    label: "Probability of rain"
                  ),
                ],
                height: 250
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
    double interval = 5;

    if (metricToDisplay == "temperature") {
      maxMetric = "realMax";
      minMetric = "realMin";
    }
    else if (metricToDisplay == "precipitation") {
      maxMetric = minMetric = "proba";
      interval = 10;
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
    final roundedMinY = (minYValue / 5).floor() * 5 - 5;
  
    double finalMaxY = roundedMaxY.toDouble();
    if (metricToDisplay == "precipitation") {
      finalMaxY = 100.0;
    }
    final finalMinY = roundedMinY < 0 ? 0 : roundedMinY;

    return LineChartData(
      minX: 0,
      maxX: 7,
      minY: finalMinY.toDouble(),
      maxY: finalMaxY.toDouble(),
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
            reservedSize: 55, // leave enough space for numbers
            interval: interval,
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
            String display;

            if (metricToDisplay == "temperature") {
              display = widget.metrics["temperature"] == 0 ? "º" : "F";
            } else if (metricToDisplay == "wind") {
              display = widget.metrics["wind"] == 0 ? "km/h" : (widget.metrics["wind"] == 1 ? "m/s" : "Knots");
            }
            else {
              display = "%";
            }

            return LineTooltipItem(
              '${spot.y.toStringAsFixed(1)} $display',
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
          color: Colors.yellow,
          dotData: FlDotData(),
          spots: [
            for (var i = 0; i < 7; i++) FlSpot(i.toDouble(), parseToDouble(week[i][metricToDisplay]['realNow'])),
            FlSpot(7, parseToDouble(today[metricToDisplay]['realNow'])),
          ],
        ),
        LineChartBarData(
          color: Colors.red,
          dotData: FlDotData(),
          spots: [
            for (var i = 0; i < 7; i++) FlSpot(i.toDouble(), parseToDouble(week[i][metricToDisplay][maxMetric])),
            FlSpot(7, parseToDouble(today[metricToDisplay][maxMetric])),
          ],
        ),
        LineChartBarData(
          color: blue,
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
          color: blue,
          dotData: FlDotData(),
          spots: [
            for (var i = 0; i < 7; i++) FlSpot(i.toDouble(), parseToDouble(week[i][metricToDisplay][minMetric])),
            FlSpot(7, parseToDouble(today[metricToDisplay][minMetric])),
          ],
        ),
      ];
    }
  }
}
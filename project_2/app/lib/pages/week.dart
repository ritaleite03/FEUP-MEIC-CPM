import 'package:app/pages/widgets/utils.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class WeekPage extends StatelessWidget {
  final String? cityName;
  // ignore: prefer_typing_uninitialized_variables
  final today;
  // ignore: prefer_typing_uninitialized_variables
  final week;

  WeekPage({
    super.key,
    required this.cityName,
    required this.today,
    required this.week,
  });

  final List<String> weekDays = [
    "Mon",
    "Tue",
    "Wed",
    "Thu",
    "Fri",
    "Sat",
    "Sun",
  ];
  late final List<String> weekday = _generateWeekdays();

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
    }
    throw Exception('Valor não é nem int nem double: $value');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.primary,
      appBar: AppBarWidget(title: cityName ?? ''),
      body: Padding(
        padding: const EdgeInsets.all(8),
        child: Column(
          children: [
            ContainerWidget(
              child: SizedBox(
                height: 200,
                child: LineChart(_buildChartDataTemperature(context)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  LineChartData _buildChartDataTemperature(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    print(week);
    return LineChartData(
      minY: 0,
      maxY:
          [
            parseToDouble(week[0]['temperature']['realMax']),
            parseToDouble(week[1]['temperature']['realMax']),
            parseToDouble(week[2]['temperature']['realMax']),
            parseToDouble(week[3]['temperature']['realMax']),
            parseToDouble(week[4]['temperature']['realMax']),
            parseToDouble(week[5]['temperature']['realMax']),
            parseToDouble(week[6]['temperature']['realMax']),
            parseToDouble(today['temperature']['realMax']),
          ].reduce((a, b) => a > b ? a : b) +
          10,
      gridData: FlGridData(
        getDrawingHorizontalLine:
            (value) => FlLine(
              // ignore: deprecated_member_use
              color: colorScheme.surfaceBright.withOpacity(0.1),
              strokeWidth: 1,
            ),
      ),
      titlesData: FlTitlesData(
        topTitles: AxisTitles(),
        rightTitles: AxisTitles(),
        leftTitles: AxisTitles(),
        bottomTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            getTitlesWidget: (val, meta) => _buildBottom(val, context),
          ),
        ),
      ),
      borderData: FlBorderData(show: false),
      lineBarsData: _buildLineBars(context),
      lineTouchData: LineTouchData(
        touchTooltipData: LineTouchTooltipData(
          tooltipPadding: const EdgeInsets.all(10),
          getTooltipItems:
              (touchedSpots) =>
                  touchedSpots.map((LineBarSpot touchedSpot) {
                    return LineTooltipItem(
                      '${touchedSpot.y.toStringAsFixed(1)} F',
                      TextStyle(color: colorScheme.surfaceBright),
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

  List<LineChartBarData> _buildLineBars(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return [
      LineChartBarData(
        color: colorScheme.primary,
        dotData: FlDotData(),
        spots: [
          FlSpot(0, parseToDouble(week[0]['temperature']['realMax'])),
          FlSpot(1, parseToDouble(week[1]['temperature']['realMax'])),
          FlSpot(2, parseToDouble(week[2]['temperature']['realMax'])),
          FlSpot(3, parseToDouble(week[3]['temperature']['realMax'])),
          FlSpot(4, parseToDouble(week[4]['temperature']['realMax'])),
          FlSpot(5, parseToDouble(week[5]['temperature']['realMax'])),
          FlSpot(6, parseToDouble(week[6]['temperature']['realMax'])),
          FlSpot(7, parseToDouble(today['temperature']['realMax'])),
        ],
      ),
      LineChartBarData(
        color: colorScheme.secondary,
        dotData: FlDotData(),
        spots: [
          FlSpot(0, parseToDouble(week[0]['temperature']['realMin'])),
          FlSpot(1, parseToDouble(week[1]['temperature']['realMin'])),
          FlSpot(2, parseToDouble(week[2]['temperature']['realMin'])),
          FlSpot(3, parseToDouble(week[3]['temperature']['realMin'])),
          FlSpot(4, parseToDouble(week[4]['temperature']['realMin'])),
          FlSpot(5, parseToDouble(week[5]['temperature']['realMin'])),
          FlSpot(6, parseToDouble(week[6]['temperature']['realMin'])),
          FlSpot(7, parseToDouble(today['temperature']['realMin'])),
        ],
      ),
    ];
  }
}

String celsiusToFahrenheit(dynamic temperature) {
  double celsius;

  if (temperature is String) {
    celsius = double.parse(temperature);
  }
  else if (temperature is int) {
    celsius = temperature.toDouble();
  }
  else if (temperature is double) {
    celsius = temperature;
  }
  else {
    throw Exception("Unsupported type: ${temperature.runtimeType}");
  }

  return ((celsius * 1.8) + 32).toStringAsFixed(0);
}

String kmhToMps(dynamic velocity) {
  double kmh;

  if (velocity is String) {
    kmh = double.parse(velocity);
  }
  else if (velocity is int) {
    kmh = velocity.toDouble();
  }
  else if (velocity is double) {
    kmh = velocity;
  }
  else {
    throw Exception("Unsupported type: ${velocity.runtimeType}");
  }

  return (kmh / 3.6).toStringAsFixed(2);
}

String kmhToKnots(dynamic velocity) {
  double kmh;

  if (velocity is String) {
    kmh = double.parse(velocity);
  }
  else if (velocity is int) {
    kmh = velocity.toDouble();
  }
  else if (velocity is double) {
    kmh = velocity;
  }
  else {
    throw Exception("Unsupported type: ${velocity.runtimeType}");
  }

  return (kmh * 0.539957).toStringAsFixed(2);
}

// from hPa
String pressureToInches(dynamic pressure) {
  double hPa;

  if (pressure is String) {
    hPa = double.parse(pressure);
  }
  else if (pressure is int) {
    hPa = pressure.toDouble();
  }
  else if (pressure is double) {
    hPa = pressure;
  }
  else {
    throw Exception("Unsupported type: ${pressure.runtimeType}");
  }

  return (hPa * 0.02953).toStringAsFixed(2);
}

// from hPa
String pressureToKPA(dynamic pressure) {
  double hPa;

  if (pressure is String) {
    hPa = double.parse(pressure);
  }
  else if (pressure is int) {
    hPa = pressure.toDouble();
  }
  else if (pressure is double) {
    hPa = pressure;
  }
  else {
    throw Exception("Unsupported type: ${pressure.runtimeType}");
  }

  return (hPa * 0.1).toStringAsFixed(2);
}

// from hPa
String pressureToMM (dynamic pressure) {
  double hPa;

  if (pressure is String) {
    hPa = double.parse(pressure);
  }
  else if (pressure is int) {
    hPa = pressure.toDouble();
  }
  else if (pressure is double) {
    hPa = pressure;
  }
  else {
    throw Exception("Unsupported type: ${pressure.runtimeType}");
  }

  return (hPa * 0.75006157584566).toStringAsFixed(2);
}
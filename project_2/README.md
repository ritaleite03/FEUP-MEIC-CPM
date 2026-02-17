# Weather Forecast Application – Project 2

## 📌 Project Overview

A **weather application** for district capitals in Portugal, offering:

- **Weather Forecast:** Current day and next day predictions.
- **Weekly Overview:** Charts for temperature, wind speed, and precipitation probability.
- **Favorites:** Users can add cities for quick access, with persistent local storage.
- **Settings:** Customize units (Celsius/Fahrenheit), saved locally.

**Components:**

1. **Server (Node.js):** Handles API requests for weather data.
2. **Client App (Flutter):** Android/iOS app for user interaction.

> ⚠️ **Note:** The full, detailed report for this project is available in [`report.md`](report.md).

## 🗂 Project Structure

```
project/
├── server/           # Node.js backend
├── app/              # Flutter mobile app
├── images/           # UI screenshots and architecture diagrams
├── README.md         # This file
└── report.md         # Full detailed report
```

## ⚙ Setup

1. Define `.env` inside `app/` folder with:

```text
IP=<server_ip_address>
```

2. Start the server:

```bash
cd server
npm install
node server.js
```

3. Launch the Flutter app:

```bash
cd app
flutter run
```

> Ensure the server IP matches the device running the app.

## 🛠 Key Features

- Search and add district capitals as favorites
- View current and next-day weather forecasts
- Weekly charts for temperature, wind, and rain
- Persist favorites and settings locally
- Handle server unavailability gracefully

## 📚 Libraries & Tools

| Technology   | Purpose                               |
| ------------ | ------------------------------------- |
| **Flutter**  | Cross-platform mobile app development |
| **Node.js**  | Backend server for API requests       |
| **sqflite**  | Persistent storage for favorites      |
| **fl_chart** | Charts for weekly weather trends      |

# Smart Supermarket Environment – Project 1

## 📌 Project Overview

A **smart supermarket system** integrating four components:

1. **Server:** HTTP REST API handling user registration, payments, transactions, and vouchers.
2. **Customer App (Android):** Register, view transactions, retrieve vouchers, shop with QR/NFC scanning, checkout.
3. **Checkout Terminal App (Android):** Receive customer cart, process payment, show results.
4. **QR Code Generator App (Android):** Generate QR codes for products with UUID, category, and price.

**Key Features:**

- User authentication with RSA & EC keys
- Product browsing, cart management, checkout with discounts/vouchers
- Local data persistence for users, vouchers, transactions
- Secure communication with the server
- Dark/light mode for all Android apps

> ⚠️ **Note:** The full, detailed report for this project is available in [`report.md`](report.md).

## 🗂 Project Structure

```
project/
├── server/           # Node.js server code
├── generator/        # QR code generator app
├── client/           # Customer Android app
├── terminal/         # Checkout Android app
├── images/           # Architecture, navigation, UI screenshots
├── README.md         # This file
└── report.md         # Full detailed report
```

## ⚙ Setup

1. Change `SERVER_IP` in `Constants.kt` for **server, terminal, and generator apps**.
2. Start the server:

```bash
cd server
npm install
node server.js
```

3. Launch the **QR Code Generator App**
4. Launch the **Customer App** and/or **Checkout Terminal App**

> Ensure the **supermarket key** remains unchanged once customers register.

## 🛠 Technologies Used

| Technology           | Purpose                            |
| -------------------- | ---------------------------------- |
| **Node.js**          | Server-side REST API               |
| **Android (Kotlin)** | Customer, Terminal, Generator apps |
| **RSA/EC Keys**      | Secure authentication & encryption |
| **SQLite**           | Local data persistence on Android  |
| **QR Codes / NFC**   | Product checkout and communication |

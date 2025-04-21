# M.EIC 2024/2025 CPM - 1st Assignment

## Team

By Group 5:

-   Henrique Gonçalves Graveto Futuro da Silva (202105647)
-   Rita Isabel Guedes Correia Leite (202105309)
-   Tiago Miguel Seixas de Azevedo (202108699)

## Index

1. [Overview](#overview)
2. [How To Use](#how-to-use)
3. [Architecture](#architecture)
4. [Data Schemas](#data-schemas)
5. [Implemented Features](#implemented-features)

    5.1. [On the Server](#on-the-server)

    5.2. [On the QR Code Generator App](#on-the-qr-code-generator-app)

    5.3. [On the Terminal App](#on-the-terminal-app)

    5.4. [On the User App](#on-the-user-app)

6. [Navigation](#navigation)
7. [Security](#security)
8. [Performed Tests](#performed-tests)

## Overview

The goal of this project was to develop three integrated systems for a smart supermarket environment.

The first system is a remote service, implemented as an HTTP REST API, hosted on the supermarket's server. This service is organized into several groups of operations, including customer registration and validation, checkout processing, transaction consultation, and voucher retrieval.

The second system is an Android application designed for customers. This app allows users to register in the system, view previous transactions, retrieve vouchers, and initiate a shopping session. During shopping, customers can add items to their virtual basket by scanning tags or QR codes on the products. When ready to check out, they can complete the purchase directly through the app, optionally applying a previously retrieved voucher for a discount.

The third system involves the supermarket checkout terminals, which also run an Android application. These terminals receive the list of products, and optionally a voucher, sent from the customer’s mobile app. This information is communicated to the server, which processes the payment and returns the result of the transaction along with the total amount charged. If the transmitted product list matches the physical contents of the basket, the terminal authorizes the opening of the exit gates (note that this gate mechanism was not implemented in this proof of concept).

## How To Use

To begin using the system, follow the steps below in the specified order to ensure proper communication between components:

1. **Start the Server:**  
   Open a terminal window and navigate to the directory containing the `server.js` file. Once there, start the server by running the following command:

    ```
    node server.js
    ```

2. **Launch the QR Code Generator Application:**  
   After the server is up and running, install or open the application responsible for generating QR codes for the products. This step must be performed after starting the server because the QR code generator needs to inform the server of its identification key (referred to as the "supermarket key").

3. **Launch the Customer Application:**  
   Finally, install or open the customer-facing Android application. When a new user registers through the app, the server responds with two important pieces of information: the customer's UUID and the supermarket key.

**Important Note:**  
It is crucial that the supermarket key remains unchanged after a customer has registered. Since there is no implemented mechanism for the server to notify the customer of any changes to the supermarket key, altering it could lead to communication failures or inconsistencies in the system.

## Architecture

## Data Schemas

## Implemented Features

### On the Server

-   Receives the public key from the supermarket, sent by the QR code generator app.
-   Registers users, providing them with their UUID and with the public key of the supermarket.

### On the QR Code Generator App

-   Generates an RSA key pair.
-   Sends the public key to the server.
-   Creates QR codes for products, including their UUID, name and price.

### On the Terminal App

-   Reads checkout messages via QR Code or NFC.
-   Sends the checkout request to the server.
-   Displays the server's response to the user.

### On the User App

-   **Authentication**

    -   Generates RSA and EC key pairs on the device.
    -   Registers the user on the server and securely stores credentials locally.
    -   Supports local login if the user is already registered. - Provides user feedback during login and registration in case of errors.

-   **Product and Cart Management**

    -   Adds products to the cart by scanning QR codes.
    -   Removes products from the cart.
    -   Sort products by selection date, name or price.
    -   Filter products by their category.
    -   Filter products by their name.
    -   Checkout configuration allows:

        -   Choosing the payment method: NFC or QR Code;
        -   Enabling or disabling discounts;
        -   Selecting a voucher (or choosing none).

-   **Payment**

    -   Generates the checkout message via QR Code or NFC.
    -   Executes the payment process.
    -   Provide the option to go back and continue shopping (payment was not completed, so the product list remains unchanged).
    -   Provide the option to confirm that the payment was completed (payment was successful, so the product list is cleared).

-   **Transactions, Vouchers and Discount**

    -   Displays recent transactions, available vouchers and discount.
    -   Requests transactions, vouchers and discount from the server using a nonce challenge for secure authentication.

-   **Data Persistence**

    -   Stores the user's product list locally in a database.
    -   Stores the user's vouchers locally in a database.
    -   Stores the user's discount locally in a database.
    -   Stores the user's transactions locally in a database. // TODO

## Navigation

## Security

## Performed Tests

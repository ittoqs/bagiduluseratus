# 📱 Offline File Share

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

A native Android application written in Kotlin for sharing files between devices without requiring an internet connection. It utilizes Android's Wi-Fi Direct (P2P) technology for high-speed, local data transfer.

---

## ✨ Features

* **🌐 100% Offline Transfer:** No internet, cellular data, or external Wi-Fi routers needed.
* **⚡ Wi-Fi Direct:** Uses Android's built-in `WifiP2pManager` to seamlessly discover and connect to nearby peers.
* **🏗️ Simple Architecture:** Built using standard, lightweight components (`Activity`, `BroadcastReceiver`, `IntentService`, `Coroutines`) ensuring straightforward readability and minimal external dependencies.

---

## 🛠️ Setup Instructions

Follow these steps to get the project running locally:

1. **Clone & Open:** Clone this repository and open the project in **Android Studio**.
2. **Gradle Sync:** Allow Android Studio to sync the Gradle files and download required dependencies.
3. **Deploy:** Run the app on **two physical Android devices**. 

> [!NOTE]  
> Wi-Fi Direct requires physical hardware wireless chips and is notoriously difficult or impossible to test accurately on standard Android emulators.

---

## 🚀 Usage Guide

1. **Launch:** Open the app on both physical devices.
2. **Permissions:** Grant the necessary permissions when prompted (`Location` / `Nearby Devices` depending on your Android version).
3. **Discovery:** Tap **"Discover Peers"** on one of the devices.
4. **Connect:** Select the target device from the discovered list to initiate a connection handshake.
5. **Role Assignment:** Once connected, Android's P2P framework automatically assigns one device as the **Group Owner (Server)** and the other as the **Client**.
6. **Transfer:** *(WIP)* Tap **"Send File"** to pick a file from your storage and stream it safely over the socket connection.

---

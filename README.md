# 📱 NotifySim (Notification Simulator)

A native Android application designed to simulate authentic incoming notifications for **WhatsApp** and **Google Messages (SMS)** with customizable sender names / phone numbers, custom messages, immediate testing, and custom date/time scheduling.

---

## ✨ Features

- **Multi-Platform Simulation**:
  - **WhatsApp Mode**: Authentic WhatsApp styling, vector logo (`ic_whatsapp`), circular green badge, and quick actions (*Reply*, *Mark as read*).
  - **Google Messages Mode**: Google blue styling, Google Messages chat icon, support for SMS / phone numbers, and automatic *Copy Code* action for OTP / verification messages.
- **Custom Date & Time Scheduling**: Schedule notifications for any exact future date and time with native DatePicker & TimePicker dialogs, or use quick delays (5s, 10s, 30s, 1m, 5m).
- **Launcher vs Notification App Name**:
  - App displays as **`NotifySim`** in your phone's app drawer and home screen.
  - Notifications display with **`Messages`** in the system notification header.
- **Modern Material 3 Theme & Dark Mode**:
  - Full DayNight support with an instant ☀️/🌙 Dark Mode toggle in the app bar.
  - Interactive **Live Notification Preview** card on the home screen that updates in real-time as you type.
- **Instant Test Button**: Fires a notification immediately. Supports auto-minimizing the app to home screen so the notification banner drops down from the top just like an authentic incoming chat.
- **Android 13+ & 14+ Ready**: Built-in runtime permission handling for `POST_NOTIFICATIONS` and exact alarms.

---

## 🚀 Building & Installation

### Option 1: Automated GitHub Actions
Every push to `main` automatically triggers the GitHub Actions workflow to build the APK.
1. Go to the **Actions** tab in this repository.
2. Click on the latest workflow run.
3. Download the `Notification-Simulator-v1.4.0-APK` artifact.
4. Alternatively, check the **Releases** tab for direct `.apk` downloads.

### Option 2: Local Build
```bash
git clone https://github.com/wizzwuz/whatsapp-notification-simulator.git
cd whatsapp-notification-simulator
./gradlew assembleDebug
```
The output APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`.

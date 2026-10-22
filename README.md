# 📱 WhatsApp Notification Simulator

A native Android application designed to simulate incoming WhatsApp notifications with custom sender names, custom message texts, immediate testing, and delay-based scheduling.

---

## ✨ Features

- **Custom Sender Name**: Input any sender name or select from quick presets (*Mom ❤️*, *Boss 💼*, *Bank Alert 🏦*, *Crush ✨*).
- **Custom Message Body**: Input any custom message text or choose from quick sample messages.
- **WhatsApp Notification Styling**:
  - Accurate WhatsApp vector logo icon for both small status bar icon and large colored badge.
  - Subtext displaying `"WhatsApp"`.
  - Realistic quick actions: `"Reply"` and `"Mark as read"`.
  - High importance heads-up banner with sound and vibration.
- **Instant Test Button**: Fires a notification immediately. Supports auto-minimizing the app to home screen so the notification banner drops down from the top just like an authentic incoming chat.
- **Schedule Message Button**: Uses Android's `AlarmManager.setExactAndAllowWhileIdle()` to schedule notifications to fire after a customizable delay (5s, 10s, 30s, 1m, 5m), even if the app is in the background or the screen is locked. Includes real-time countdown status and cancellation support.
- **Android 13+ & 14+ Ready**: Built-in runtime permission handling for `POST_NOTIFICATIONS` and exact alarms.

---

## 🛠️ Tech Stack & Architecture

- **Platform**: Android Native (Kotlin)
- **Compile SDK**: 34 (Android 14) / Min SDK: 26 (Android 8.0 Oreo)
- **UI Architecture**: Material Design 3 (ViewBinding, MaterialComponents)
- **Background Scheduling**: `AlarmManager` with `BroadcastReceiver` (`NotificationPublisherReceiver`)
- **CI / CD**: Automated GitHub Actions workflow compiling the debug APK and publishing artifacts / releases.

---

## 🚀 Building & Installation

### Option 1: Automated GitHub Actions
Every push to `main` automatically triggers the GitHub Actions workflow to build the APK.
1. Go to the **Actions** tab in this repository.
2. Click on the latest workflow run.
3. Download the `WhatsApp-Notification-Simulator-APK` artifact.
4. Alternatively, check the **Releases** tab for direct `.apk` downloads.

### Option 2: Local Build
```bash
git clone https://github.com/wizzwuz/whatsapp-notification-simulator.git
cd whatsapp-notification-simulator
./gradlew assembleDebug
```
The output APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`.

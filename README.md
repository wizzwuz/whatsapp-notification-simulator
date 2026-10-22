# 📱 Notification Simulator (WhatsApp & Google Messages)

A native Android application designed to simulate authentic incoming notifications for **WhatsApp** and **Google Messages (SMS)** with customizable sender names / phone numbers, custom messages, immediate testing, and delay-based scheduling.

---

## ✨ Features

- **Multi-Platform Simulation**:
  - **WhatsApp Mode**: Authentic WhatsApp styling, vector logo (`ic_whatsapp`), circular green badge, and quick actions (*Reply*, *Mark as read*).
  - **Google Messages Mode**: Google blue styling, Google Messages chat icon, support for SMS / phone numbers, and automatic *Copy Code* action for OTP / verification messages.
- **Custom Sender Name & Presets**: Input any sender name / phone number or tap quick presets (*Mom ❤️*, *Boss 💼*, *Google*, *Uber*, *+1 555-019-2834*).
- **Custom Message Body & Templates**: Input any custom message text or choose from quick sample messages (chat conversations, OTP verification codes, delivery notifications).
- **Clean Notifications**: Zero simulator branding in system notifications. The notification header matches the platform name authentically.
- **Dedicated Launcher Icon**: Clean, modern app icon (`ic_launcher_sim`) that doesn't disguise itself as WhatsApp on your home screen / app drawer.
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
3. Download the `Notification-Simulator-v1.1.0-APK` artifact.
4. Alternatively, check the **Releases** tab for direct `.apk` downloads.

### Option 2: Local Build
```bash
git clone https://github.com/wizzwuz/whatsapp-notification-simulator.git
cd whatsapp-notification-simulator
./gradlew assembleDebug
```
The output APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`.

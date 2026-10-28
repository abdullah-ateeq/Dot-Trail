# Dot Trail 🔴🔵

> **Connect the dots before time runs out!**

Dot Trail is a fast-paced, visually engaging Android reaction and numerical sequencing puzzle game built modernly with **Jetpack Compose** and **Material 3**. Players tap numbered dots in exact ascending order (`1 → 2 → 3 → ...`) against a ticking countdown timer.

---

## ✨ Features

- 🎮 **3 Difficulty Modes**: Easy, Medium, and Hard with scaling dot counts and stricter time limits.
- ⏱️ **Real-time Gameplay & Urgency**: Precision timer with warning pulses when low on time, and time penalty popups on wrong taps.
- 🎲 **Procedural Level Generation**: Fair, non-overlapping dot placement engine utilizing distance-based sampling with intelligent fallback grid layout for small screens.
- 🎨 **Responsive Canvas & Visuals**: Custom Canvas rendering, smooth animated trailing line connections, and celebratory victory particle effects.
- 🔊 **Synthesized Sound & Haptics**: Pure procedural PCM audio synth (`AudioTrack`) and custom vibration haptics without heavy external assets.
- 📊 **Comprehensive Game Stats**: Track personal best scores, fastest completion times, current & longest win streaks, and accuracy breakdown per difficulty.
- 🌓 **Theming & Accessibility**: Light, Dark, and System theme support, custom timer styles (Digital/Compact), and a Reduce Motion option.
- 🔒 **100% Offline & Privacy First**: No ads, no analytics, no accounts required—all data stays strictly on your device.

---

## 🎮 How to Play

1. **Start a Level**: Choose your preferred difficulty (Easy, Medium, Hard) and tap **PLAY**.
2. **Find Dot 1**: Locate the target dot numbered `1`.
3. **Tap in Sequence**: Tap `1 → 2 → 3 → 4 → ...` in exact numerical order.
4. **Watch the Clock**: Connect all dots before the timer runs out.
5. **Avoid Mistakes**: Tapping the wrong dot incurs a time penalty (-0.5s on Easy, -1.0s on Medium, -1.5s on Hard).

---

## 📸 Screen Breakdown

- **Home Screen**: Select difficulty, view high scores & streak records, view mini animated game demo.
- **Game Screen**: Interactive Canvas board, real-time timer countdown, progress bar, mistake tracker, pause overlay.
- **Result Screen**: Detailed performance breakdown (Accuracy, Completion Time, Score, Performance Stars, New Personal Records).
- **Settings Screen**: Theme selection, audio/haptic toggles, timer display options, motion reduction, and statistics reset.
- **About Screen**: Gameplay rules, feature overview, and project info.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (2.2.10)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: MVVM with Unidirectional Data Flow (UDF), `ViewModel`, and `StateFlow`
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Graphics**: Compose `Canvas` drawing API & custom animation vectors
- **Persistence**: Android `SharedPreferences` via `PreferencesManager`
- **Audio & Feedback**: Custom PCM Tone Synthesis via `AudioTrack` and Android `Vibrator` APIs

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 or higher
- Android SDK 24+ (Android 7.0 / Nougat minimum)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Dot-Trail.git
   cd Dot-Trail
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run on an Android device or emulator running API 24+.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

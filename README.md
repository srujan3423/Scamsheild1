# ScamShield AI 🛡️

**ScamShield AI** is an Android security application designed to help users identify potentially suspicious messages and phone numbers.

## Features

- 📱 SMS scam analysis
- ☎️ Incoming call screening using Android's call-screening framework
- 🚩 Suspicious phone-number reporting
- 📋 Screened call history
- 🔔 Warnings for previously reported suspicious callers
- 🗂️ Local storage for scan and call history

*Only describe features that are implemented and tested in the current version.*

## Tech Stack

- Kotlin
- Android Studio
- Jetpack Compose
- Android CallScreeningService
- SharedPreferences and JSON for local storage
- Gradle

## Project Structure

The project contains Android UI screens, call-screening logic, scan storage, and the application entry point.

## Setup Instructions

1. Clone or download this repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync and install the required dependencies.
4. Run the application on a compatible Android device.
5. If testing call screening, follow Android's requirements for selecting the app as the default call-screening application.

## Testing

Test SMS analysis, suspicious-number reporting, incoming call screening, and call-history storage separately. Actual call-screening behavior depends on Android permissions, device configuration, and system support.

## Limitations

- Scam detection is not guaranteed to identify every scam.
- A caller being reported as suspicious does not prove that the caller is fraudulent.
- Call-screening results depend on Android's system behavior and app configuration.

## Future Improvements

- Improved scam-detection models
- URL reputation checks
- Better user reporting and feedback
- Security and usability testing
- Optional backend integration

## Disclaimer

ScamShield AI is a security-assistance project. Users should independently verify suspicious calls and messages before taking action.

# Money Tracker Android

Native Android wrapper around the Money Tracker local web UI.

- Loads the UI from Android app assets using `file:///android_asset/index.html`.
- Uses Android AlarmManager for the daily reminder; no continuous background service.
- Transactions remain local on the device.
- Android build is produced with Gradle/GitHub Actions.

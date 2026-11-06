# Amaru Ghar (અમારું ઘર), Our Home

A private family app for a couple: home expenses and budgets, savings dreams, shopping and chore lists, reminders (bills, EMI, insurance, vehicle, birthdays, anniversary), medicines and health records, a document vault, memories, love notes and an emergency card. Gujarati and English, works online and offline.

[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE) ![Languages: Gujarati + English](https://img.shields.io/badge/languages-Gujarati%20%2B%20English-blue)

**Live demo (phone and PC, no install):** https://1darshankumarsavsani-blip.github.io/Amaru-Ghar/

## Features (V1)

- Home expenses with categories, monthly budgets with an 80% warning, savings and dream goals with progress bars
- Shopping and grocery list, chores (me / wife / both)
- Reminders: bill, EMI, insurance, vehicle PUC or service, birthday, anniversary, doctor (once, monthly or yearly)
- Medicines with times, doctor visits and a family health record
- Vault and memories for documents and photos (kept on the device only)
- Anniversary countdown, love-note cards, emergency card with tap-to-call numbers
- Android: two home-screen widgets (Today and Shopping list), reminder notifications, fingerprint unlock, Google sync
- Themes, dark mode, text size, bold and high contrast; PIN lock; backup and restore; CSV and PDF reports

## Download (latest release)

- `Amaru_Ghar_V1.apk`: Android app (the whole app is inside, works offline)
- `Amaru_Ghar_Windows.exe`: Windows launcher (opens the website in its own window; Edge or Chrome needed)

See the [Releases page](../../releases/latest).

## Tech stack

- Web: single-file HTML/CSS/JavaScript, no libraries; IndexedDB for files; service worker for offline use
- Android: Java WebView wrapper with native widgets, alarms, notifications and fingerprint (BiometricPrompt)
- Sync: Firebase Authentication (Google) and the Firestore REST API

## Project structure

```text
docs/     website (index.html, sync.js, app-auth.html, manifest, service worker)
app/      Android app (Java) with the same page in app/src/main/assets
release/  release files
.github/  issue templates and release workflow
```

## Build from source

Open `app/` in Android Studio, or run `./gradlew testDebugUnitTest` and `./gradlew assembleRelease` (JDK 17, Android SDK 34; sign with your own keystore). The website needs no build: open `docs/index.html`.

## Privacy

Everything stays on your device. Documents and photos never leave the device. Only if you sign in with Google does the text data sync to your own Google account's document.

## Honest notes

Tested in a headless browser and on a simulated Android 14 phone only. Widgets, reminders, fingerprint and Google sign-in have not been run on a real phone yet. The live link with a wife's phone (shared data) is planned for V2.

## License

MIT, see [LICENSE](LICENSE).

## Author

Darshankumar Savsani, DARSHAN CORPORATION, Rajkot. [GitHub](https://github.com/1darshankumarsavsani-blip) / [DARSHAN-CROP](https://github.com/DARSHAN-CROP)

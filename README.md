# Edge Home · Android

The Android app for Edge Home, a local smart-home system that runs on a Raspberry Pi hub and is controlled through a gateway API, with an AI assistant that operates devices through chat.

**Status: prototype build.** The app ships the full interface with simulated devices. It does not talk to the hub yet.

## Install on your phone

1. Open the [latest release](../../releases/latest) on your phone.
2. Download `EdgeHome-<n>.apk`.
3. Open it. If Android asks, allow installs from your browser or files app.

Every push to `main` builds a new APK automatically (Actions → Build APK) and publishes it as a release.

## What's in the app

- **Home**: one room at a time, with room tabs, a readings strip and device cards for lights, fan, AC, blinds and curtains, the front door lock, sensors and outlets.
- **Chat**: ask the assistant to change things. Replies stream in, each action shows as a chip, and the lock always asks you to confirm.
- **Admin**: approve new nodes with a name and a room. Users and rooms, and the action log, come next.
- **Settings**: switch between admin, member and guest (view-only), switch theme, and simulate a blind timeout or an offline lamp.

Hard rules the UI follows:

1. Locks look different from normal devices (risk color, shield label, no switch).
2. Offline devices are dimmed and can't be controlled.
3. Guests never see a control that wouldn't work for them.
4. No tokens, hashes or internal IDs are ever shown.
5. Dark and light themes with WCAG AA contrast, on phone, tablet and desktop.

## Project layout

```
app/src/main/assets/www/   the interface (HTML, CSS, JS, bundled fonts)
app/src/main/java/...      MainActivity: hosts the interface, handles Back
design/                    design tokens and the design system notes
.github/workflows/         builds the APK on every push
```

The interface is served from inside the APK at `https://appassets.androidplatform.net/`, so it opens instantly and works offline.

## Build locally

You need JDK 17 and the Android SDK (Android Studio installs both).

```
gradle :app:assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`.

## Next

- Wire the app to the hub's gateway API (sign-in, rooms and devices, live state, chat stream, confirmations).
- Admin: users and rooms, action log.
- Edge states: loading, empty and error.
- A native Jetpack Compose UI built from the same design tokens in `design/tokens.json`.

# Edge Home · Android

The Android app for Edge Home. It opens the real Edge Home dashboard served by your hub's gateway in a full-screen WebView. Nothing is bundled: the dashboard comes from the hub, so dashboard updates need no new APK.

## Install on your phone

1. Open the [latest release](../../releases/latest) on your phone.
2. Download `EdgeHome-<n>.apk` and open it. If Android asks, allow installs from your browser or files app.
3. On first launch, confirm the gateway address (default `https://homebench.tailffe052.ts.net`) and tap Connect.

Your phone must reach the hub: be on Tailscale (for a `*.ts.net` address) or the home network.

Every push to `main` builds a signed release APK (Actions → Build APK) and publishes it as a release. All builds use the same release key, so a new build installs over the old one.

## What the app does

- **First-run screen**: asks for the gateway address and stores it on the phone. Change it later by long-pressing the app icon → Change gateway, or from the error page.
- **Dashboard**: loads the gateway URL. Links to other sites open in the browser.
- **Pull to refresh**: pull down at the top of the page.
- **Offline page**: if the hub can't be reached, returns a server error, or has a bad certificate, the app shows a native error page with Try again and Change gateway.
- **Back button**: goes back in the dashboard's history, then leaves the app.
- **Dark and light**: status and navigation bars follow the phone's theme, and the dashboard gets the matching `prefers-color-scheme`.
- **No device permissions**: the app asks only for internet access. Camera, microphone, location, file pickers and storage are all refused. HTTPS only.

## Release signing (one-time setup)

CI signs with a fixed key stored in four repository secrets:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | The keystore file, base64-encoded |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias (`edgehome`) |
| `ANDROID_KEY_PASSWORD` | Key password |

Create the key once, on your computer (needs a JDK for `keytool` and the GitHub CLI):

```
keytool -genkeypair -v -keystore edgehome-release.jks -alias edgehome -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=Edge Home"
base64 -w0 edgehome-release.jks | gh secret set ANDROID_KEYSTORE_BASE64 -R Vedant28082005/edge-home-android
gh secret set ANDROID_KEYSTORE_PASSWORD -R Vedant28082005/edge-home-android
gh secret set ANDROID_KEY_ALIAS -R Vedant28082005/edge-home-android --body edgehome
gh secret set ANDROID_KEY_PASSWORD -R Vedant28082005/edge-home-android
```

On macOS use `base64 -i edgehome-release.jks` instead of `base64 -w0 edgehome-release.jks`. Keep `edgehome-release.jks` and its passwords backed up: without them, future builds can't update the installed app.

Pull-request builds don't need the secrets; they compile with the debug key and aren't published. Builds on `main` fail if the secrets are missing.

## Project layout

```
app/src/main/java/...      MainActivity (WebView), SetupActivity (first run), Gateway (stored URL)
app/src/main/res/          layouts, Obsidian Platinum colors, error page, launcher shortcut
design/                    tokens (CSS, JSON), design system notes, HANDOFF.md (final)
.github/workflows/         builds and publishes the signed release APK
```

## Build locally

You need JDK 17 and the Android SDK (Android Studio installs both).

```
gradle :app:assembleRelease
```

Without the signing environment variables this signs with the debug key. The APK lands in `app/build/outputs/apk/release/`.

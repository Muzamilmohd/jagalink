# JagaPMA (Jaga Phone Mak Ayah)

Formerly "JagaLink". The display name is JagaPMA, but the package/applicationId stays `com.jagalink`
(changing it would make Android treat it as a new app and break OTA updates), as do the repo name,
source folder and keystore file names.

Android app (Kotlin, no external libraries) that protects an elderly parent from scam links.
A small robot "bot" pops up over other apps with a Malay-language warning when a suspicious
link or APK file appears in WhatsApp, Telegram, SMS, Facebook, Instagram, TikTok or email.

UI text is in **Bahasa Melayu** by default (simple, friendly, for an older user), with English and
Simplified Chinese selectable in the setup screen (`Prefs.lang`). Every user-facing string goes through
`ctx.tr(ms, en, zh)` (Support.kt) or `Lang.t(...)` in `LinkAnalyzer` — always add all three. In Kotlin
templates next to Chinese text use `${name}`, not `$name` (Han characters count as identifier letters).

## Architecture
- `LinkAnalyzer.kt` – pure-Kotlin rule engine (no Android imports). Scores links 0–100:
  >=60 DANGER, >=30 WARN. Lists to extend: `BRANDS`, `TRUSTED`, `SCAM_WORDS`,
  `SUSPICIOUS_TLDS`, `SHORTENERS`. All checks run on-device; never send user messages anywhere.
  Adult sites (`ADULT_SITES`, `ADULT_WORDS`, `ADULT_TOKENS`, `ADULT_TLDS`) score 100 with
  `Verdict.adult = true` — hard-blocked: the bot card hides the "Buka juga" option.
- `Watchers.kt` – `ScreenWatcher` (AccessibilityService, scans screen text + clicked views, throttled 1.5 s). Optional in setup
  (shown under "Pilihan" with ⚪ when off) — without it the link guard still checks every tapped link.
  There is deliberately **no** NotificationListenerService (removed in v1.4): the app must not read
  incoming messages/notifications — only what is on screen and links the user taps.
- `LinkGuardActivity.kt` – registered as a browser. When set as default browser every tapped link
  passes through it: SAFE → forwarded to the real browser (`Browsers.pick`), else bot screen.
- `BotOverlay.kt` – `WarnCenter` (dedupe per URL/source), `BotOverlay` (TYPE_APPLICATION_OVERLAY
  window), `BotUi` (shared warning card).
- `BotView.kt` – animated robot drawn on Canvas (100×100 design grid).
- `Support.kt` – Prefs, watched app package list, TTS (ms-MY → id-ID fallback), vibration,
  fallback notification, browser picker.
- `MainActivity.kt` – setup screen (permission checklist, name/phone settings, test box).

The package list in `Watched.APPS` (Support.kt) must stay in sync with
`res/xml/accessibility_config.xml`.

**Second profiles (Samsung Dual Messenger = user 95, Secure Folder = 150):** links tapped in a cloned
WhatsApp open `LinkGuardActivity` in a *separate copy* of JagaPMA in that profile — own prefs (name,
phone), often no TTS engine. Hence `Prefs.inSecondProfile()` + the "Isi nama… WhatsApp kedua" setup
button on the card, and `Alerts.beep()` when TTS can't speak. The main-profile ScreenWatcher still
sees the cloned app's screen. Test with `adb shell am start --user 95 -n com.jagalink/.LinkGuardActivity -d <url>`.

## Build & run
- minSdk 26, targetSdk/compileSdk 35, Gradle 9.1.0, AGP 8.13.0, Kotlin 2.2.20, bytecode target 17.
- Only JDK on this machine is Android Studio's bundled JBR 25 (`C:\Program Files\Android\Android Studio\jbr`),
  hence the newer Gradle/AGP/Kotlin. Set `JAVA_HOME` to it. If Gradle fails with
  "Unable to establish loopback connection", set `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\gtmp`.
- `local.properties` (gitignored) holds `sdk.dir=C:/Users/Mohd Muzamil/AppData/Local/Android/Sdk` — use forward slashes.
  - Build: `./gradlew assembleDebug`
  - Install on connected phone: `./gradlew installDebug` (or `adb install -r app/build/outputs/apk/debug/app-debug.apk`)
  - Logs: `adb logcat | grep -i jagalink`
- Quick engine test without Android: compile `LinkAnalyzer.kt` with `kotlinc` and run sample messages.

## Release & OTA update
- `Updater.kt` checks `https://api.github.com/repos/<jagalink.updateRepo>/releases/latest` (repo set in
  `gradle.properties`, currently `Muzamilmohd/jagalink`), daily via `UpdateJob` + on app open, and installs
  the release's `.apk` asset through `PackageInstaller`. Only release metadata/APK is fetched — no user data.
- Release key: `jagalink-release.jks` + `keystore.properties` (gitignored, must be backed up). Every OTA APK
  must be signed with it, otherwise the install fails (signature mismatch).
- To release: bump `versionCode` + `versionName` in `app/build.gradle.kts`, `./gradlew assembleRelease`,
  copy to `dist/JagaPMA-v<ver>.apk`, then `gh release create v<ver> dist/JagaPMA-v<ver>.apk --notes "..."`.
  Tag must be `v` + versionName.
- `REQUEST_INSTALL_PACKAGES` (self-update) is not allowed on Google Play — strip it and the Updater for a Play build.

## Status
- LinkAnalyzer tested with 18 sample messages (scam + normal) — correct.
- Debug build compiles cleanly and installs/launches on a Mi 10 (2026-10-27). Overlay, services and
  browser role not yet tested on the device.

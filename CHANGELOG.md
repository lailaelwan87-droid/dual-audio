# Changelog — سجل التغييرات

All notable changes to this modified derivative of **Dual Audio** (`com.dualaudio.app`) are documented in this file in accordance with **GNU GPL-3.0-or-later (Section 5)**.

Original upstream project: **xTudoS/dual-audio** (<https://github.com/xTudoS/dual-audio>, `Copyright (C) 2026 xTudoS`).

---

## [2.0.1] (`versionCode 9`) — 2026-11-23

### Added / الإضافات
- **Package & Open-Source Distribution Readiness:**
  - Adopted dedicated package name and application ID `com.dualaudio.app` (distinct from upstream `com.byxnd.dualaudio`).
  - Added bilingual **Privacy Policy** (`PRIVACY_POLICY.md`, bundled `assets/legal/PRIVACY_POLICY.md`, and standalone GitHub Pages `index.html`).
  - Linked the **Privacy Policy / سياسة الخصوصية** button in the Guide/Legal screen directly to the official page (`https://lailaelwan87-droid.github.io/dual-audio-privacy/`) via the device's default browser without adding `INTERNET` permission.
  - Added verbatim GNU GPL v3.0 `LICENSE` at repository root and inside APK legal assets (`assets/legal/LICENSE`) with in-app viewer support.
  - Added bilingual `README.md` and `CHANGELOG.md` documenting original copyright (`Copyright (C) 2026 xTudoS`) and derivative modifications.
  - Configured `dependenciesInfo { includeInApk = false }` and `.gitignore` rules protecting signing keystores (`*.jks`, `*.keystore`, `*.p12`, `keystore.properties`).
- **Expanded Audio Routing, Sync & UI (relative to upstream `xTudoS/dual-audio`):**
  - Rewritten and extended routing engine (`DualOutputEngine.kt`) and Shizuku UserService (`DualOutputShizukuService.kt`) in Kotlin with support for `3.5mm` wired + Bluetooth, `USB` + Bluetooth, Auto + Bluetooth, and Custom Pair routing modes.
  - Added independent per-headphone volume controls with separate sliders, step adjustments, and mute toggles.
  - Added live AudioFlinger latency inspection, manual millisecond delay adjustment (`AudioDelayBuffer`), auto-sync latency toggle, and acoustic sync click calibrator (`AcousticSyncCalibrator`).
  - Built a full Jetpack Compose Material 3 interface with Arabic (`RTL`) and English (`LTR`) localization, Dark/Light themes, device diagnostics, and local preference persistence via Jetpack DataStore.

### Changed / التعديلات
- Updated `THIRD_PARTY_NOTICES.md` (root and APK assets) to explicitly reference the upstream repository (<https://github.com/xTudoS/dual-audio>), preserve `Copyright (C) 2026 xTudoS`, and list all GPL-3.0-compatible third-party libraries (Shizuku API under MIT; AndroidX, Jetpack Compose, Kotlin, Guava, Error Prone, and JSpecify under Apache 2.0).
- Corrected KDoc descriptions in `DualOutputEngine.kt` and `DualOutputShizukuService.kt` to accurately reflect that the implementation is derived from and extends `xTudoS/dual-audio` under `GPL-3.0-or-later`.

### Privacy & Licensing Integrity / الخصوصية والترخيص
- Verified zero `INTERNET` permission, zero dangerous/runtime permissions, zero ads, zero billing/subscription SDKs, and zero analytics or tracking libraries.
- Preserved full **GNU General Public License v3.0 or later (`GPL-3.0-or-later`)** terms across the entire derivative work.

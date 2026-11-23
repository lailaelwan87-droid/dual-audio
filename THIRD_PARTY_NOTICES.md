# License & Third-Party Notices — Dual Audio

## Application License (GPL-3.0-or-later)

Dual Audio (`com.dualaudio.app`) is free software licensed under the
**GNU General Public License v3.0 or later (`GPL-3.0-or-later`)** (see `LICENSE`).

### Original Copyright & Modification Notice (GPL-3.0 Section 5)

- Original Dual Audio project: **Copyright (C) 2026 xTudoS** (<https://github.com/xTudoS/dual-audio>), licensed under `GPL-3.0-or-later`.
- Modified and extended derivative version (`com.dualaudio.app`, modified 2026):
  - Migrated package and application ID to `com.dualaudio.app`.
  - Added Jetpack Compose Material 3 user interface with Arabic (RTL) and English (LTR) support and Dark/Light themes.
  - Added 3.5mm wired + Bluetooth, USB + Bluetooth, Auto + Bluetooth, and Custom Pair routing modes.
  - Rewrote and extended routing & Shizuku UserService in Kotlin (`DualOutputEngine`, `DualOutputShizukuService`).
  - Added independent per-headphone volume controls, live AudioFlinger latency inspection, auto-sync latency adjustment option, and Acoustic Sync Click calibrator.
  - Kept strictly free, open-source, offline (no `INTERNET` permission), with no ads, no analytics, and no tracking.

This program comes with **ABSOLUTELY NO WARRANTY**. You are welcome to redistribute and modify it
under the terms of the GNU General Public License v3.0 or later.

---

## Bundled Third-Party Libraries (GPL-3.0 Compatible)

### MIT License

**Shizuku API**: `dev.rikka.shizuku:api`, `provider`, `aidl`, `shared`
<https://github.com/RikkaApps/Shizuku-API>

```
MIT License

Copyright (c) 2021 RikkaW

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Apache License 2.0

Full text: <https://www.apache.org/licenses/LICENSE-2.0.txt>

- **AndroidX & Jetpack Compose** (core-ktx, activity-compose, lifecycle, datastore-preferences, compose-ui, material3, material-icons), Copyright The Android Open Source Project
- **Kotlin Standard Library & kotlinx.coroutines**, Copyright JetBrains s.r.o. and Kotlin Programming Language contributors
- **Guava ListenableFuture** (`com.google.guava:listenablefuture`), Copyright The Guava Authors
- **Error Prone annotations** (`com.google.errorprone:error_prone_annotations`), Copyright Google LLC
- **JSpecify** (`org.jspecify:jspecify`), Copyright The JSpecify Authors

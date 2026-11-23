# Dual Audio — الصوت المزدوج

**Application ID:** `com.dualaudio.app`  
**Current Version:** `2.0.1` (`versionCode 9`)  
**License:** GNU General Public License v3.0 or later (`GPL-3.0-or-later`)  
**Privacy Policy:** <https://lailaelwan87-droid.github.io/dual-audio-privacy/>

> **Attribution & Derivative Work Notice (GPL-3.0 Section 5):**  
> This project is a **modified and extended derivative version** based on the original open-source **Dual Audio** project by **xTudoS** (<https://github.com/xTudoS/dual-audio>, `Copyright (C) 2026 xTudoS`), licensed under **GNU GPL-3.0-or-later**. It is an independent community-modified edition with its own package ID (`com.dualaudio.app`) and is not an official release by the original author.

---

## العربية

### نظرة عامة
**Dual Audio (الصوت المزدوج)** هو تطبيق Android مجاني ومفتوح المصدر لتشغيل صوت الوسائط على **سماعتين في وقتٍ واحد** (مثل سماعة سلكية `3.5mm` أو سماعة `USB` مع سماعة بلوتوث) دون الحاجة لصلاحيات Root، بالاعتماد على خدمة **Shizuku** المحلية.

### الميزات الرئيسية
- **أوضاع توجيه متعددة:**
  - سماعة سلكية (`3.5mm` / `AUX`) + بلوتوث
  - سماعة `USB` + بلوتوث
  - الوضع التلقائي (سلكي أو USB) + بلوتوث
  - اختيار يدوي لأي مخرجين صوتيين متصلين (`Custom Pair`)
- **التحكم المستقل بمستوى الصوت:** شريط تحكم منفصل لكل سماعة مع أزرار الرفع والخفض والكتم على حدة.
- **قياس ومعايرة التأخير (Latency):** قياس فرق التأخير بين السماعتين، مع خيار الضبط التلقائي أو التعديل اليدوي بالملي ثانية ونبضة معايرة سمعية.
- **إعادة التفعيل التلقائي:** خدمة خلفية عبر Shizuku تراقب اتصال السماعتين وتعيد تطبيق التوجيه تلقائيًا عند إعادة الاتصال.
- **واجهة حديثة ثنائية اللغة:** مبنية بـ Jetpack Compose (Material 3) مع دعم كامل للعربية (`RTL`) والإنجليزية (`LTR`) والمظهر الداكن والفاتح.

### الخصوصية والأمان
- يعمل التطبيق **محليًا بالكامل بدون اتصال بالإنترنت** ولا يطلب إذن `INTERNET` ولا أي أذونات وقت التشغيل الخطرة.
- **مجاني 100%:** لا يحتوي على إعلانات، ولا اشتراكات، ولا أدوات تتبع أو تحليلات (`Analytics` / `Crashlytics`).
- يحفظ تفضيلات الواجهة والصوت غير الشخصية محليًا فقط عبر Jetpack DataStore.
- للاطلاع على التفاصيل الكاملة: [سياسة الخصوصية الرسمية](https://lailaelwan87-droid.github.io/dual-audio-privacy/) أو ملف [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md).

### المتطلبات وطريقة التشغيل
1. تثبيت تطبيق **Shizuku** من [GitHub Releases](https://github.com/RikkaApps/Shizuku/releases) أو Google Play وتشغيله عبر **تصحيح الأخطاء اللاسلكي (Wireless Debugging)**.
2. فتح **Dual Audio** ومنحه إذن Shizuku (`السماح دائمًا`).
3. توصيل السماعة السلكية (`3.5mm` أو `USB`) وسماعة البلوتوث، ثم الضغط على **تفعيل الصوت المزدوج**.

### البناء من الشيفرة المصدرية
- متطلبات البناء: JDK 17+ وAndroid SDK.
- لبناء نسخة التطوير (Debug):
  ```bash
  gradle :app:assembleDebug
  ```
- لتشغيل اختبارات الوحدة وRobolectric:
  ```bash
  gradle :app:testDebugUnitTest
  ```
- لبناء نسخة الإصدار الموقّعة (Release): وفّر مفتاح التوقيع عبر متغيرات البيئة (`KEYSTORE_PATH` و`STORE_PASSWORD` و`KEY_PASSWORD`) دون حفظ أي مفاتيح أو كلمات مرور داخل المستودع، ثم نفّذ:
  ```bash
  gradle :app:assembleRelease
  ```

### الحقوق والترخيص
- المشروع الأصلي: **Copyright (C) 2026 xTudoS** — <https://github.com/xTudoS/dual-audio>
- النسخة المعدّلة (`com.dualaudio.app`): مرخّصة بالكامل بموجب **GNU General Public License v3.0 or later (`GPL-3.0-or-later`)** وتأتي **بدون أي ضمان**.
- راجع الملفات القانونية المرفقة:
  - [`LICENSE`](LICENSE) — النص الحرفي الكامل لرخصة GNU GPL v3.0
  - [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) — إشعارات الحقوق والتعديلات والمكتبات الخارجية
  - [`CHANGELOG.md`](CHANGELOG.md) — سجل التغييرات للإصدار الحالي (`2.0.1`)
- للتواصل بشأن الخصوصية أو الترخيص: <mailto:Lailaelwan87@gmail.com>

---

## English

### Overview
**Dual Audio** (`com.dualaudio.app`) is a free, open-source Android application that routes media audio to **two output devices simultaneously** (such as `3.5mm` wired or `USB` headphones together with a Bluetooth headset) without root access, using the local **Shizuku** service.

### Key Features
- **Multiple Routing Modes:**
  - `3.5mm` Wired / AUX + Bluetooth
  - `USB` Audio + Bluetooth
  - Auto (`3.5mm` or `USB`) + Bluetooth
  - Custom Pair (any two connected audio outputs)
- **Independent Per-Headphone Volume Controls:** Separate sliders, step buttons, and mute controls for each output channel.
- **Latency Measurement & Calibration:** Inspects latency difference between outputs, with an auto-sync option, manual millisecond offset adjustment, and an acoustic sync click calibrator.
- **Automatic Reapply:** Shizuku-hosted background monitor automatically restores dual routing when headphones reconnect.
- **Bilingual Material 3 UI:** Built with Jetpack Compose supporting Arabic (`RTL`) and English (`LTR`) plus Dark and Light themes.

### Privacy & Security
- **Strictly Offline:** Declares no `INTERNET` permission and no runtime/dangerous permissions.
- **100% Free & Tracker-Free:** Contains no ads, no subscriptions, no analytics, and no crash reporting SDKs.
- Stores only non-personal app preferences locally inside Android's private Jetpack DataStore.
- Read the full policy online: [Official Privacy Policy](https://lailaelwan87-droid.github.io/dual-audio-privacy/) or [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md).

### Requirements & Usage
1. Install and start **Shizuku** ([GitHub Releases](https://github.com/RikkaApps/Shizuku/releases) or Google Play) using **Wireless Debugging**.
2. Open **Dual Audio** and grant Shizuku permission (*Allow all the time*).
3. Connect your wired (`3.5mm` or `USB`) headphones and Bluetooth headset, then tap **Enable Dual Audio**.

### Building from Source
- Requirements: JDK 17+ and Android SDK.
- Build a Debug APK:
  ```bash
  gradle :app:assembleDebug
  ```
- Run unit and Robolectric tests:
  ```bash
  gradle :app:testDebugUnitTest
  ```
- Build a signed Release APK by providing signing credentials strictly via environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`)—never commit keystores or passwords:
  ```bash
  gradle :app:assembleRelease
  ```

### Copyright, Attribution & License
- **Original Project:** Copyright (C) 2026 xTudoS — <https://github.com/xTudoS/dual-audio>
- **Modified Derivative (`com.dualaudio.app`):** Licensed as a whole under **GNU General Public License v3.0 or later (`GPL-3.0-or-later`)** and provided **WITHOUT ANY WARRANTY**.
- Included legal & release documentation:
  - [`LICENSE`](LICENSE) — Verbatim GNU GPL v3.0 license text
  - [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) — Copyright notices, modification log, and third-party library licenses (MIT & Apache 2.0)
  - [`CHANGELOG.md`](CHANGELOG.md) — Release notes for version `2.0.1`
- Contact for privacy or licensing inquiries: <mailto:Lailaelwan87@gmail.com>

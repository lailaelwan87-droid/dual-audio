# Privacy Policy — Dual Audio
**سياسة الخصوصية — تطبيق الصوت المزدوج (Dual Audio)**

- **Application ID / معرّف التطبيق:** `com.dualaudio.app`
- **License / الترخيص:** GNU General Public License v3.0 or later (`GPL-3.0-or-later`)
- **Last Updated / آخر تحديث:** November 22, 2026

---

## العربية

### 1. مقدمة (Introduction)
توضح سياسة الخصوصية هذه كيفية تعامل تطبيق **Dual Audio (الصوت المزدوج)** (`com.dualaudio.app`) مع المعلومات والبيانات على جهازك. تطبيق Dual Audio هو تطبيق Android مجاني ومفتوح المصدر مرخّص بموجب رخصة **GPL-3.0-or-later**، مصمّم لتشغيل صوت الوسائط عبر سماعتين معاً (مثل سماعة سلكية 3.5mm أو USB مع سماعة بلوتوث) مع تحكم مستقل في مستوى صوت كل سماعة وقياس/ضبط التأخير محلياً على جهازك فقط.

### 2. جمع المعلومات (Information Collection)
**لا يجمع تطبيق Dual Audio أي بيانات شخصية أو معلومات تعريفية عنك.**
وللشفافية الكاملة، نميّز بوضوح بين **الوصول المحلي** و**الجمع** و**الإرسال**:
- **البيانات التي يتم جمعها (Collected):** لا شيء. لا يجمع التطبيق أسماء أو عناوين بريد إلكتروني أو معرّفات إعلانية أو أرقام هواتف أو تسجيلات صوتية أو بيانات موقع جغرافي.
- **البيانات التي يتم الوصول إليها محلياً في الذاكرة فقط (Accessed Locally):**
  - قائمة أجهزة إخراج الصوت المتصلة حالياً عبر واجهة النظام `AudioManager` (اسم الجهاز، نوعه، ومعرّفه الداخلي أو عنوان الجهاز الذي يوفره النظام) لغرض عرض السماعات المتصلة وتوجيه الصوت إليها.
  - خصائص تدفق الصوت المحلي (معدل العينة، حجم المخزن المؤقت، درجات مستوى صوت الوسائط `STREAM_MUSIC`) وبيانات حالة التوجيه وزمن التأخير (`AudioFlinger`) عند تفعيل Shizuku، وذلك في الذاكرة المؤقتة فقط أثناء التشغيل.
- **البيانات المرسلة إلى خوادم خارجية (Transmitted):** لا شيء. التطبيق لا يطلب إذن الإنترنت (`INTERNET`) ولا يتصل بأي خادم.

### 3. استخدام البيانات (Data Usage)
تُستخدم البيانات التي يصل إليها التطبيق محلياً في الذاكرة فقط لتنفيذ الوظائف الأساسية للتطبيق:
- اكتشاف السماعات السلكية (3.5mm) وUSB وسماعات البلوتوث المتصلة حالياً وعرض حالتها.
- توجيه صوت الوسائط إلى مخرجين صوتيين في وقت واحد.
- ضبط مستوى صوت كل سماعة على حدة.
- قياس فرق التأخير (Latency) ومعايرته تلقائياً أو يدوياً في أقل من ثانية.

### 4. الأذونات (Permissions)
يلتزم التطبيق بمبدأ الحد الأدنى من الأذونات:
- **لا يطلب التطبيق أي أذونات خطيرة أو أذونات وقت التشغيل** (لا يطلب الميكروفون `RECORD_AUDIO`، ولا الموقع الجغرافي، ولا الكاميرا، ولا جهات الاتصال، ولا ملفات التخزين، ولا إذن `BLUETOOTH_CONNECT`).
- **لا يطلب التطبيق إذن الإنترنت (`INTERNET`) ولا إذن حالة الشبكة (`ACCESS_NETWORK_STATE`).**
- **الأذونات والتصريحات الموجودة في ملف Manifest المدمج:**
  - `<queries>` لحزمة `moe.shizuku.privileged.api`: للتحقق مما إذا كان تطبيق Shizuku مثبّتاً وفتحه عند رغبتك.
  - `moe.shizuku.manager.permission.API_V23` (مدمج من مكتبة Shizuku API): لطلب تفويض الاتصال المحلي بخدمة Shizuku.
  - `com.dualaudio.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (إذن داخلي بمستوى `signature` مدمج من مكتبة AndroidX Core): لتأمين المستقبلات الداخلية للتطبيق.
  - حماية مكوّن `rikka.shizuku.ShizukuProvider` بإذن النظام `android.permission.INTERACT_ACROSS_USERS_FULL` لضمان عدم تمكن أي تطبيق غير مخوّل من الوصول إلى قناة الاتصال مع Shizuku.

### 5. استخدام Shizuku (Shizuku Usage)
يعتمد توجيه الصوت المزدوج والتحكم المستقل بصوت كل سماعة على **تطبيق Shizuku** (تطبيق خارجي مستقل يثبّته المستخدم ويشغّله بنفسه عبر تصحيح الأخطاء اللاسلكي أو ADB):
- يتواصل Dual Audio مع Shizuku **محلياً داخل الجهاز فقط** عبر قناة IPC (`DualOutputShizukuService`) لتنفيذ أوامر توجيه الصوت وقراءة/ضبط مستوى الصوت وقراءة زمن التأخير من خدمة الصوت في Android.
- لا يقوم Dual Audio بإرسال أي بيانات عبر Shizuku إلى الإنترنت أو إلى أي طرف خارجي.
- يمكنك إلغاء إذن Shizuku للتطبيق في أي وقت من داخل تطبيق Shizuku.

### 6. التخزين المحلي (Local Storage)
يستخدم التطبيق **Jetpack DataStore Preferences** محلياً داخل المساحة الخاصة للتطبيق (`dual_audio_prefs`) لحفظ تفضيلات الاستخدام فقط:
- لغة الواجهة (العربية أو الإنجليزية).
- وضع المظهر (داكن أو فاتح).
- وضع التوجيه المختار وترتيب المخرج الأساسي وخيار إعادة التفعيل التلقائي.
- قيمة تأخير السماعة السلكية بالملي ثانية وخيار الضبط التلقائي السريع للتأخير.
- آخر مستويات صوت مختارة للسماعة السلكية وسماعة البلوتوث.

لا يستخدم التطبيق `SharedPreferences` مباشرة ولا قواعد بيانات خارجية ولا يخزّن أي بيانات شخصية.
ملاحظة: خاصية النسخ الاحتياطي القياسية في نظام Android (`android:allowBackup="true"`) مفعّلة في ملف التطبيق، مما يعني أن نظام التشغيل قد ينسخ هذه التفضيلات غير الشخصية ضمن نسختك الاحتياطية الخاصة في Android إذا كنت قد فعّلت النسخ الاحتياطي في إعدادات جهازك.

### 7. مشاركة البيانات (Data Sharing)
لا يشارك التطبيق ولا يبيع ولا يؤجّر أي بيانات لأي جهة أو طرف ثالث.
عند ضغطك اختيارياً على زر **«نسخ التفاصيل»** داخل قسم التفاصيل التقنية المطوي، يتم نسخ نص حالة التوجيه التقنية إلى حافظة جهازك المحلية فقط بناءً على طلبك المباشر، دون إرساله تلقائياً لأي جهة.

### 8. الخدمات الخارجية (Third-party Services)
- **لا يحتوي التطبيق على أي خدمات تحليل أو تتبع (Analytics) أو تقارير أعطال (Crash Reporting) أو إعلانات (AdMob) أو اشتراكات أو Firebase مفعّلة.**
- التفاعلان الوحيدان مع أطراف خارجية هما بإرادة المستخدم المباشرة:
  1. الاتصال المحلي على الجهاز مع تطبيق **Shizuku** (`moe.shizuku.privileged.api`) عند تثبيته وتشغيله من قِبل المستخدم.
  2. إذا لم يكن Shizuku مثبّتاً وضغطت على زر «تثبيت Shizuku»، يفتح التطبيق رابط صفحة Shizuku عبر متصفحك أو متجر التطبيقات على جهازك، ويخضع استخدامك للمتصفح أو المتجر لسياسة خصوصيته الخاصة.

### 9. أمان البيانات (Data Security)
نظراً لعدم امتلاك التطبيق لإذن الإنترنت (`INTERNET`)، وعدم جمعه لأي بيانات شخصية، واقتصار التخزين على إعدادات بسيطة داخل بيئة Android المعزولة (Sandbox)، مع تقييد مزود Shizuku بإذن `INTERACT_ACROSS_USERS_FULL`، فإن سطح التعرض للبيانات في حده الأدنى.

### 10. الاحتفاظ بالبيانات وحذفها (Data Retention and Deletion)
تبقى تفضيلاتك المحلية محفوظة على جهازك فقط طالما التطبيق مثبّت. يمكنك حذفها بالكامل في أي وقت عبر:
- **إعدادات Android ← التطبيقات ← Dual Audio ← التخزين ← مسح البيانات (Clear Data)**، أو
- **إلغاء تثبيت التطبيق** من جهازك.

### 11. خصوصية الأطفال (Children's Privacy)
نظراً لأن تطبيق Dual Audio لا يجمع أي معلومات شخصية من أي مستخدم، فإنه لا يجمع عن علم أو دون علم أي بيانات شخصية من الأطفال دون سن 13 عاماً (أو السن القانوني المعمول به في بلدك).

### 12. تحديثات السياسة (Policy Updates)
قد نقوم بتحديث سياسة الخصوصية هذه عند إضافة ميزات جديدة أو إجراء تغييرات تقنية في التطبيق. سيتم نشر أي نسخة محدّثة مع تاريخ التحديث الجديد في مستودع المشروع وداخل التطبيق في الإصدارات المستقبلية.

### 13. معلومات التواصل (Contact Information)
إذا كانت لديك أي أسئلة حول سياسة الخصوصية هذه، يمكنك التواصل معنا عبر:
- **البريد الإلكتروني / وسيلة التواصل:** *(تُضاف لاحقاً)*

---

## English

### 1. Introduction
This Privacy Policy explains how **Dual Audio** (`com.dualaudio.app`) handles information on your device. Dual Audio is a free, open-source Android application distributed under the **GNU General Public License v3.0 or later (`GPL-3.0-or-later`)** that routes media audio to two outputs simultaneously (such as 3.5mm wired or USB headphones alongside a Bluetooth headset) with independent per-headphone volume controls and sub-second latency measurement/calibration—operating strictly locally on your device.

### 2. Information Collection
**Dual Audio does not collect any personal data or personally identifiable information.**
For complete clarity, we distinguish between **collection**, **local access**, and **transmission**:
- **Data Collected:** None. The app does not collect names, email addresses, advertising IDs, phone numbers, audio recordings, or location data.
- **Data Accessed Locally (In-Memory Only):**
  - Currently connected audio output devices via Android's `AudioManager` API (device name, device type, internal device ID, and device address when exposed by the OS) strictly to display connected headphones and apply dual routing.
  - Local audio output properties (sample rate, frames per buffer, `STREAM_MUSIC` volume step count) and, when Shizuku is connected, audio routing status, per-device volume index, and `AudioFlinger` latency lines—processed strictly in volatile memory on your device.
- **Data Transmitted Externally:** None. The app does not declare the `INTERNET` permission and never connects to any external server.

### 3. Data Usage
Information accessed locally in memory is used exclusively to provide the app's core functionality:
- Detecting and displaying connected 3.5mm wired, USB, and Bluetooth audio outputs.
- Routing media playback simultaneously to two connected audio devices.
- Adjusting each headphone's volume independently.
- Measuring and calibrating Bluetooth-to-wired audio latency in under one second.

### 4. Permissions
Dual Audio follows strict least-privilege design:
- **No runtime or dangerous Android permissions are requested** (no `RECORD_AUDIO`, no location, no camera, no contacts, no storage/media permissions, and no `BLUETOOTH_CONNECT`).
- **No `INTERNET` or `ACCESS_NETWORK_STATE` permission is declared.**
- **Manifest declarations present in the merged AndroidManifest.xml:**
  - `<queries>` for `moe.shizuku.privileged.api`: package visibility required on Android 11+ to check whether Shizuku is installed and launch it.
  - `moe.shizuku.manager.permission.API_V23` (merged from `dev.rikka.shizuku:api`): allows requesting local Shizuku binder authorization.
  - `com.dualaudio.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (internal `signature`-level permission merged from AndroidX Core): protects internal app broadcast receivers.
  - `rikka.shizuku.ShizukuProvider` protected by `android.permission.INTERACT_ACROSS_USERS_FULL`: restricts access to the Shizuku binder provider so unauthorized apps cannot bind to it.

### 5. Shizuku Usage
Dual audio routing and independent per-device volume control rely on **Shizuku**, an independent third-party app installed and started locally by the user (e.g., via Wireless Debugging or ADB):
- Dual Audio communicates with Shizuku **strictly on-device via local IPC** (`DualOutputShizukuService`) to apply combined audio routing, adjust per-device volume indices, and read `AudioFlinger` latency figures.
- No personal data or diagnostic output is transmitted over any network through Shizuku.
- You may revoke Dual Audio's Shizuku authorization at any time inside the Shizuku app.

### 6. Local Storage
Dual Audio uses **Jetpack DataStore Preferences** inside Android's private app storage (`dual_audio_prefs`) solely to remember your app preferences:
- Interface language (Arabic or English).
- Theme preference (Dark or Light).
- Selected routing mode, primary output priority, and background auto-reapply toggle.
- Wired delay offset in milliseconds and instant auto-sync latency toggle.
- Last selected volume levels for wired/USB and Bluetooth channels.

No direct `SharedPreferences`, SQLite/Room databases, or personal identifiers are stored.
Note: Standard Android Auto Backup (`android:allowBackup="true"`) is enabled in the manifest, meaning the Android OS may include these non-personal preference settings in your personal Android device backup if you have enabled device backup in Android system settings.

### 7. Data Sharing
We do not share, sell, rent, or disclose any data to third parties.
If you explicitly tap **"Copy Details"** inside the collapsed technical diagnostics section, the technical routing status text is copied solely to your device's local clipboard at your request and is never sent anywhere automatically.

### 8. Third-party Services
- **Dual Audio contains no analytics, no crash reporting SDKs, no advertising SDKs (no AdMob), no subscriptions/billing SDKs, and no active Firebase services.**
- The only external interactions are strictly user-initiated:
  1. On-device local IPC with the user-installed **Shizuku** app (`moe.shizuku.privileged.api`).
  2. If Shizuku is not installed and you tap **"Install Shizuku"**, the app opens the Shizuku listing URL via your device's browser or store app, which is governed by that browser or store's own privacy policy.

### 9. Data Security
Because Dual Audio has no `INTERNET` permission, collects no personal information, keeps all preferences inside Android's private application sandbox, and restricts its Shizuku provider with `INTERACT_ACROSS_USERS_FULL`, data exposure risk is minimized by design.

### 10. Data Retention and Deletion
Local app preferences remain on your device only while Dual Audio is installed. You can permanently delete all stored preferences at any time via:
- **Android Settings → Apps → Dual Audio → Storage → Clear data**, or
- **Uninstalling Dual Audio** from your device.

### 11. Children's Privacy
Because Dual Audio does not collect personal information from any user, it does not knowingly collect personal information from children under 13 (or the applicable age of digital consent in your jurisdiction).

### 12. Policy Updates
We may update this Privacy Policy if new features or technical changes affect how the app operates. Any updated version will be published in the project repository and included in future app releases with an updated "Last Updated" date.

### 13. Contact Information
If you have questions regarding this Privacy Policy, you may contact us at:
- **Contact Email / Channel:** *(To be added)*

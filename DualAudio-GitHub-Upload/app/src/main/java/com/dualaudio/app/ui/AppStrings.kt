package com.dualaudio.app.ui

import com.dualaudio.app.audio.AppLanguage
import com.dualaudio.app.audio.OutputCategory
import com.dualaudio.app.audio.RoutingMode
import com.dualaudio.app.audio.ShizukuStatus

data class UiStrings(
    val appTitle: String,
    val navConsole: String,
    val navSync: String,
    val navDevices: String,
    val navGuide: String,
    val languageButtonLabel: String,
    val toggleThemeCd: String,
    val refreshCd: String,

    // Shizuku status banner (consumer-friendly)
    val shizukuReadyTitle: String,
    val shizukuReadyDesc: String,
    val shizukuPermissionTitle: String,
    val shizukuPermissionDesc: String,
    val shizukuGrantBtn: String,
    val shizukuNotRunningTitle: String,
    val shizukuNotRunningDesc: String,
    val shizukuNotInstalledTitle: String,
    val shizukuNotInstalledDesc: String,
    val shizukuOpenBtn: String,
    val shizukuInstallBtn: String,
    val shizukuConnectingTitle: String,
    val shizukuConnectingDesc: String,
    val shizukuTooOldTitle: String,
    val shizukuTooOldDesc: String,
    val androidTooOldTitle: String,
    val androidTooOldDesc: String,

    // Friendly routing status banners
    val bannerDualEnabled: String,
    val bannerDualDisabled: String,
    val bannerWaitingWired: String,
    val bannerWaitingUsb: String,
    val bannerWaitingBluetooth: String,
    val bannerWaitingPair: String,
    val bannerGenericError: String,

    // Visualizer & Slots
    val visualizerActiveTitle: String,
    val visualizerWaitingTitle: String,
    val visualizerStandbyTitle: String,
    val channelALabel: String,
    val channelBLabel: String,
    val connectedBadge: String,
    val disconnectedBadge: String,
    val waitingWiredHint: String,
    val waitingUsbHint: String,
    val waitingAutoHint: String,
    val waitingBluetoothHint: String,
    val selectTwoHint: String,
    val quickSyncBannerTitle: String,
    val quickSyncBannerCta: String,
    val autoSyncActiveChip: String,

    // Routing Modes
    val routingModeSectionTitle: String,
    val modeWiredBt: String,
    val modeWiredBtDesc: String,
    val modeUsbBt: String,
    val modeUsbBtDesc: String,
    val modeAutoBt: String,
    val modeAutoBtDesc: String,
    val modeCustom: String,
    val modeCustomDesc: String,
    val customPickerTitle: String,

    // Controls
    val enableDualAudioBtn: String,
    val disableDualAudioBtn: String,
    val testSoundBtn: String,
    val stopTestSoundBtn: String,
    val advancedOptionsTitle: String,
    val autoReapplyTitle: String,
    val autoReapplyDesc: String,
    val primaryOrderTitle: String,
    val primaryOrderWiredFirst: String,
    val primaryOrderBtFirst: String,
    val primaryOrderDesc: String,

    // Sync & Delay Screen
    val latencyTelemetryTitle: String,
    val latencyTelemetrySubtitle: String,
    val metricWiredLabel: String,
    val metricBluetoothLabel: String,
    val metricDeltaLabel: String,
    val measureLatencyBtn: String,
    val measureAndAutoSyncBtn: String,
    val applyMeasuredDeltaBtn: String,
    val autoSyncCardTitle: String,
    val autoSyncCardDesc: String,
    val autoSyncSpeedPrefix: String,
    val delayControlTitle: String,
    val delayControlExplain: String,
    val calibratorTitle: String,
    val calibratorDesc: String,
    val startCalibratorBtn: String,
    val stopCalibratorBtn: String,

    // Independent Per-Headphone Volume Mixer
    val volumeMixerTitle: String,
    val volumeMixerSubtitle: String,
    val volumeIndependentBadge: String,
    val volumeWiredChannelLabel: String,
    val volumeBluetoothChannelLabel: String,
    val volumeStepDownCd: String,
    val volumeStepUpCd: String,
    val volumeMuteCd: String,
    val volumeShizukuRequiredHint: String,

    // Devices & Diagnostics Screen
    val detectedOutputsTitle: String,
    val detectedOutputsSubtitle: String,
    val noExternalDevicesHint: String,
    val technicalDetailsToggle: String,
    val shellNotConnectedLog: String,
    val copyLogBtn: String,
    val copiedToast: String,
    val refreshDiagnosticsBtn: String,

    // Guide & Legal Screen
    val guideHeroTitle: String,
    val guideHeroSubtitle: String,
    val step1Title: String,
    val step1Body: String,
    val step2Title: String,
    val step2Body: String,
    val step3Title: String,
    val step3Body: String,
    val step4Title: String,
    val step4Body: String,
    val tipsSectionTitle: String,
    val tipWiredTitle: String,
    val tipWiredBody: String,
    val tipLatencyTitle: String,
    val tipLatencyBody: String,
    val tipSampleRateTitle: String,
    val tipSampleRateBody: String,
    val legalSectionTitle: String,
    val privacyPolicyBtn: String,
    val gplLicenseBtn: String,
    val openSourceNoticesBtn: String,
    val closeBtn: String,

    // Category labels
    val catWired35mm: String,
    val catUsbAudio: String,
    val catBluetooth: String,
    val catBuiltinSpeaker: String,
    val catOther: String
)

fun stringsFor(language: AppLanguage): UiStrings = when (language) {
    AppLanguage.ARABIC -> ArabicStrings
    AppLanguage.ENGLISH -> EnglishStrings
}

fun UiStrings.modeTitle(mode: RoutingMode): String = when (mode) {
    RoutingMode.WIRED_BLUETOOTH -> modeWiredBt
    RoutingMode.USB_BLUETOOTH -> modeUsbBt
    RoutingMode.AUTO_BLUETOOTH -> modeAutoBt
    RoutingMode.CUSTOM -> modeCustom
}

fun UiStrings.modeDescription(mode: RoutingMode): String = when (mode) {
    RoutingMode.WIRED_BLUETOOTH -> modeWiredBtDesc
    RoutingMode.USB_BLUETOOTH -> modeUsbBtDesc
    RoutingMode.AUTO_BLUETOOTH -> modeAutoBtDesc
    RoutingMode.CUSTOM -> modeCustomDesc
}

fun UiStrings.categoryLabel(category: OutputCategory): String = when (category) {
    OutputCategory.WIRED_35MM -> catWired35mm
    OutputCategory.USB_AUDIO -> catUsbAudio
    OutputCategory.BLUETOOTH -> catBluetooth
    OutputCategory.BUILTIN_SPEAKER -> catBuiltinSpeaker
    OutputCategory.OTHER -> catOther
}

fun UiStrings.shizukuTitle(status: ShizukuStatus): String = when (status) {
    ShizukuStatus.READY -> shizukuReadyTitle
    ShizukuStatus.PERMISSION_REQUIRED -> shizukuPermissionTitle
    ShizukuStatus.SHIZUKU_NOT_RUNNING -> shizukuNotRunningTitle
    ShizukuStatus.SHIZUKU_NOT_INSTALLED -> shizukuNotInstalledTitle
    ShizukuStatus.CONNECTING_SERVICE -> shizukuConnectingTitle
    ShizukuStatus.SHIZUKU_TOO_OLD -> shizukuTooOldTitle
    ShizukuStatus.ANDROID_TOO_OLD -> androidTooOldTitle
}

fun UiStrings.shizukuDesc(status: ShizukuStatus): String = when (status) {
    ShizukuStatus.READY -> shizukuReadyDesc
    ShizukuStatus.PERMISSION_REQUIRED -> shizukuPermissionDesc
    ShizukuStatus.SHIZUKU_NOT_RUNNING -> shizukuNotRunningDesc
    ShizukuStatus.SHIZUKU_NOT_INSTALLED -> shizukuNotInstalledDesc
    ShizukuStatus.CONNECTING_SERVICE -> shizukuConnectingDesc
    ShizukuStatus.SHIZUKU_TOO_OLD -> shizukuTooOldDesc
    ShizukuStatus.ANDROID_TOO_OLD -> androidTooOldDesc
}

private val ArabicStrings = UiStrings(
    appTitle = "الصوت المزدوج",
    navConsole = "التحكم",
    navSync = "التزامن",
    navDevices = "الأجهزة",
    navGuide = "الدليل",
    languageButtonLabel = "EN",
    toggleThemeCd = "تبديل المظهر الفاتح والداكن",
    refreshCd = "تحديث الأجهزة وحالة التوجيه",

    shizukuReadyTitle = "خدمة التوجيه الصوتي متصلة وجاهزة",
    shizukuReadyDesc = "يمكنك الآن تشغيل الصوت على سماعتين معاً والتحكم بكل سماعة على حدة.",
    shizukuPermissionTitle = "مطلوب منح إذن Shizuku",
    shizukuPermissionDesc = "اضغط لمنح التطبيق إذن التحكم بتوجيه الصوت المزدوج بدون روت.",
    shizukuGrantBtn = "منح الإذن",
    shizukuNotRunningTitle = "تطبيق Shizuku غير مشغّل",
    shizukuNotRunningDesc = "افتح Shizuku وابدأ الخدمة عبر تصحيح الأخطاء اللاسلكي ثم عد إلى هنا.",
    shizukuNotInstalledTitle = "تطبيق Shizuku مطلوب للتشغيل",
    shizukuNotInstalledDesc = "ثبّت تطبيق Shizuku المجاني لتشغيل الصوت المزدوج بدون روت.",
    shizukuOpenBtn = "فتح Shizuku",
    shizukuInstallBtn = "تثبيت Shizuku",
    shizukuConnectingTitle = "جارٍ الاتصال بخدمة التوجيه...",
    shizukuConnectingDesc = "يرجى الانتظار لحظة بينما يتم تجهيز الاتصال.",
    shizukuTooOldTitle = "يرجى تحديث Shizuku",
    shizukuTooOldDesc = "إصدار Shizuku الحالي قديم؛ قم بتحديثه إلى أحدث إصدار.",
    androidTooOldTitle = "إصدار النظام غير مدعوم",
    androidTooOldDesc = "ميزة توجيه الصوت المزدوج تتطلب إصداراً أحدث من النظام.",

    bannerDualEnabled = "تم تفعيل الصوت المزدوج على السماعتين بنجاح",
    bannerDualDisabled = "تم إيقاف الصوت المزدوج واستعادة الوضع العادي",
    bannerWaitingWired = "بانتظار توصيل السماعة السلكية (3.5mm) — سيعمل التوجيه تلقائياً عند التوصيل",
    bannerWaitingUsb = "بانتظار توصيل سماعة USB — سيعمل التوجيه تلقائياً عند التوصيل",
    bannerWaitingBluetooth = "بانتظار توصيل سماعة البلوتوث — سيعمل التوجيه تلقائياً عند التوصيل",
    bannerWaitingPair = "يرجى توصيل السماعتين معاً لتفعيل البث المزدوج",
    bannerGenericError = "تعذر تطبيق التوجيه حالياً؛ تأكد من اتصال السماعتين وShizuku",

    visualizerActiveTitle = "البث المزدوج نشط الآن",
    visualizerWaitingTitle = "بانتظار توصيل السماعتين...",
    visualizerStandbyTitle = "وضع الاستعداد — إخراج عادي",
    channelALabel = "السماعة الأولى",
    channelBLabel = "السماعة الثانية",
    connectedBadge = "متصل",
    disconnectedBadge = "غير متصل",
    waitingWiredHint = "وصّل السماعة السلكية (منفذ 3.5mm)",
    waitingUsbHint = "وصّل سماعة أو محول USB",
    waitingAutoHint = "وصّل سماعة سلكية 3.5mm أو USB",
    waitingBluetoothHint = "وصّل سماعة بلوتوث",
    selectTwoHint = "اختر جهازين من القائمة بالأسفل",
    quickSyncBannerTitle = "قياس وضبط تأخير البلوتوث تلقائياً في أقل من ثانية",
    quickSyncBannerCta = "التزامن",
    autoSyncActiveChip = "ضبط تلقائي نشط",

    routingModeSectionTitle = "وضع الإخراج المزدوج",
    modeWiredBt = "سلكية 3.5mm + بلوتوث",
    modeWiredBtDesc = "إخراج الصوت معاً عبر السماعة السلكية (3.5mm/AUX) وسماعة البلوتوث.",
    modeUsbBt = "سماعة USB + بلوتوث",
    modeUsbBtDesc = "إخراج الصوت معاً عبر منفذ USB-C وسماعة البلوتوث.",
    modeAutoBt = "تلقائي + بلوتوث",
    modeAutoBtDesc = "يكتشف تلقائياً السماعة السلكية أو USB ويدمجها مع البلوتوث.",
    modeCustom = "اختيار يدوي",
    modeCustomDesc = "اختر أي مخرجين صوتيين متصلين حالياً لتشغيل الصوت عليهما معاً.",
    customPickerTitle = "اختر مخرجين متصلين:",

    enableDualAudioBtn = "تفعيل الصوت المزدوج",
    disableDualAudioBtn = "إيقاف (الوضع العادي)",
    testSoundBtn = "اختبار الصوت على السماعتين",
    stopTestSoundBtn = "إيقاف صوت الاختبار",
    advancedOptionsTitle = "الإعدادات المتقدمة",
    autoReapplyTitle = "إعادة التفعيل تلقائياً في الخلفية",
    autoReapplyDesc = "يعيد تفعيل الصوت المزدوج تلقائياً عند إعادة اتصال السماعة أو البلوتوث.",
    primaryOrderTitle = "أولوية المخرج الأساسي",
    primaryOrderWiredFirst = "السلكية / USB أولاً",
    primaryOrderBtFirst = "البلوتوث أولاً",
    primaryOrderDesc = "على بعض الأجهزة يقلل تغيير الترتيب من التقطيع أو اختلاف التردد.",

    latencyTelemetryTitle = "قياس مدة التأخير الفعلية (Latency)",
    latencyTelemetrySubtitle = "يحسب فرق التوقيت بين السماعة السلكية والبلوتوث بالملي ثانية.",
    metricWiredLabel = "السلكية 3.5mm",
    metricBluetoothLabel = "البلوتوث",
    metricDeltaLabel = "فرق التأخير (Δ)",
    measureLatencyBtn = "قياس التأخير الآن",
    measureAndAutoSyncBtn = "قياس وضبط فوري (< 1ث)",
    applyMeasuredDeltaBtn = "تطبيق الفرق الآن",
    autoSyncCardTitle = "الضبط التلقائي السريع للتأخير (< 1 ثانية)",
    autoSyncCardDesc = "يقيس فرق التأخير ويضبطه تلقائياً في أقل من ثانية فور توصيل السماعات أو تفعيل الصوت المزدوج لمنع حدوث تأخير كبير.",
    autoSyncSpeedPrefix = "آخر ضبط تلقائي خلال",
    delayControlTitle = "معايرة فرق التأخير بالملي ثانية",
    delayControlExplain = "لأن البلوتوث يتأخر طبيعياً بـ 150–300 ms، يتم ضبط القيمة تلقائياً ويمكنك تعديلها يدوياً بدقة في أي وقت.",
    calibratorTitle = "معايرة التزامن السمعية (نبضة التطابق)",
    calibratorDesc = "ضع السماعة السلكية في أذن والبلوتوث في الأذن الأخرى، ثم شغّل النبضة وحرّك شريط الـ ms حتى تندمج النقرتان في نقرة واحدة.",
    startCalibratorBtn = "تشغيل نبضة المعايرة",
    stopCalibratorBtn = "إيقاف نبضة المعايرة",

    volumeMixerTitle = "التحكم المستقل بصوت كل سماعة",
    volumeMixerSubtitle = "كل شريط يتحكم بسماعته فقط على حدة دون التأثير على السماعة الأخرى",
    volumeIndependentBadge = "تحكم مستقل",
    volumeWiredChannelLabel = "السماعة السلكية (3.5mm / USB)",
    volumeBluetoothChannelLabel = "سماعة البلوتوث",
    volumeStepDownCd = "خفض مستوى الصوت درجة واحدة",
    volumeStepUpCd = "رفع مستوى الصوت درجة واحدة",
    volumeMuteCd = "كتم أو إلغاء كتم هذه السماعة فقط",
    volumeShizukuRequiredHint = "فعّل اتصال Shizuku لتطبيق مستوى صوت مستقل لكل سماعة.",

    detectedOutputsTitle = "السماعات المتصلة حالياً",
    detectedOutputsSubtitle = "تحديث فوري تلقائي عند توصيل أو فصل السماعات",
    noExternalDevicesHint = "لم يتم اكتشاف سماعات خارجية بعد — وصّل سماعة 3.5mm سلكية وسماعة بلوتوث.",
    technicalDetailsToggle = "تفاصيل تقنية للدعم الفني",
    shellNotConnectedLog = "الخدمة غير متصلة حالياً. شغّل Shizuku لعرض تفاصيل التشخيص.",
    copyLogBtn = "نسخ التفاصيل",
    copiedToast = "تم النسخ إلى الحافظة",
    refreshDiagnosticsBtn = "تحديث القائمة",

    guideHeroTitle = "دليل التشغيل السريع بدون روت",
    guideHeroSubtitle = "اتبع هذه الخطوات البسيطة لتشغيل الصوت على سماعتين معاً والتحكم المستقل في كل سماعة.",
    step1Title = "1. تثبيت وتشغيل Shizuku",
    step1Body = "فعّل خيارات المطور ثم (تصحيح الأخطاء اللاسلكي - Wireless Debugging) أثناء الاتصال بشبكة Wi-Fi، وقم بتشغيل Shizuku.",
    step2Title = "2. منح الإذن للتطبيق",
    step2Body = "من تبويب التحكم، اضغط على (منح الإذن) واختر (السماح دائماً) لربط التطبيق.",
    step3Title = "3. توصيل السماعة السلكية والبلوتوث",
    step3Body = "أدخل السماعة السلكية في منفذ 3.5mm وتأكد من اتصال سماعة البلوتوث. ستظهر علامة «متصل» لكليهما.",
    step4Title = "4. التفعيل والتحكم بصوت كل سماعة",
    step4Body = "اضغط (تفعيل الصوت المزدوج)، وسيقوم التطبيق تلقائياً بضبط التزامن في أقل من ثانية ويمكنك التحكم بصوت كل سماعة على حدة.",
    tipsSectionTitle = "ملاحظات هامة",
    tipWiredTitle = "توافق منفذ 3.5mm السلكي",
    tipWiredBody = "يدعم التطبيق كافة السماعات السلكية: سماعات الرأس العادية، وسماعات الميكروفون، وكابلات AUX.",
    tipLatencyTitle = "تأخير سماعات البلوتوث",
    tipLatencyBody = "تتأخر سماعات البلوتوث طبيعياً بمقدار 160–280 ms بسبب الترميز اللاسلكي؛ يقوم التطبيق بقياسه وضبطه تلقائياً في أقل من ثانية.",
    tipSampleRateTitle = "تحسين صفاء الصوت",
    tipSampleRateBody = "من خيارات المطور ← معدل عينة صوت البلوتوث، اختر 48 kHz لمطابقة السماعة السلكية ومنع التقطيع.",
    legalSectionTitle = "الخصوصية والتراخيص",
    privacyPolicyBtn = "سياسة الخصوصية",
    gplLicenseBtn = "رخصة التطبيق (GNU GPL-3.0-or-later)",
    openSourceNoticesBtn = "إشعارات الحقوق والمكتبات المفتوحة",
    closeBtn = "إغلاق",

    catWired35mm = "سماعة سلكية 3.5mm",
    catUsbAudio = "صوت USB",
    catBluetooth = "سماعة بلوتوث",
    catBuiltinSpeaker = "مكبر صوت الهاتف",
    catOther = "مخرج صوتي"
)

private val EnglishStrings = UiStrings(
    appTitle = "Dual Audio",
    navConsole = "Router",
    navSync = "Sync & Delay",
    navDevices = "Devices",
    navGuide = "Guide",
    languageButtonLabel = "عربي",
    toggleThemeCd = "Toggle light and dark theme",
    refreshCd = "Refresh connected devices and routing state",

    shizukuReadyTitle = "Audio Routing Service Ready",
    shizukuReadyDesc = "Play media to two headphones simultaneously and control each volume separately.",
    shizukuPermissionTitle = "Shizuku Permission Needed",
    shizukuPermissionDesc = "Grant access so Dual Audio can route audio to two headphones without root.",
    shizukuGrantBtn = "Grant Permission",
    shizukuNotRunningTitle = "Shizuku Is Not Running",
    shizukuNotRunningDesc = "Open Shizuku and start the service via Wireless Debugging, then return here.",
    shizukuNotInstalledTitle = "Shizuku Required",
    shizukuNotInstalledDesc = "Install the free Shizuku app to enable rootless dual audio routing.",
    shizukuOpenBtn = "Open Shizuku",
    shizukuInstallBtn = "Install Shizuku",
    shizukuConnectingTitle = "Connecting to Routing Service...",
    shizukuConnectingDesc = "Please wait a moment while the audio service connects.",
    shizukuTooOldTitle = "Please Update Shizuku",
    shizukuTooOldDesc = "Your Shizuku version is outdated. Please update to the latest release.",
    androidTooOldTitle = "Unsupported System Version",
    androidTooOldDesc = "Combined dual audio routing requires a newer OS version.",

    bannerDualEnabled = "Dual audio enabled on both headphones",
    bannerDualDisabled = "Dual audio disabled — normal playback restored",
    bannerWaitingWired = "Waiting for 3.5mm wired headphones — routing activates automatically once plugged in",
    bannerWaitingUsb = "Waiting for USB audio device — routing activates automatically once connected",
    bannerWaitingBluetooth = "Waiting for Bluetooth headset — routing activates automatically once connected",
    bannerWaitingPair = "Connect both headphones to start dual audio playback",
    bannerGenericError = "Could not apply dual routing right now. Check both headphones and Shizuku.",

    visualizerActiveTitle = "DUAL AUDIO ACTIVE",
    visualizerWaitingTitle = "WAITING FOR BOTH HEADPHONES...",
    visualizerStandbyTitle = "STANDBY — NORMAL PLAYBACK",
    channelALabel = "Headphone 1",
    channelBLabel = "Headphone 2",
    connectedBadge = "Connected",
    disconnectedBadge = "Not Connected",
    waitingWiredHint = "Plug in 3.5mm wired headphones",
    waitingUsbHint = "Plug in USB headset / DAC",
    waitingAutoHint = "Plug in 3.5mm wired or USB audio",
    waitingBluetoothHint = "Connect Bluetooth headset",
    selectTwoHint = "Select any 2 connected outputs below",
    quickSyncBannerTitle = "Sub-second auto-sync & live Bluetooth latency measurement",
    quickSyncBannerCta = "Sync",
    autoSyncActiveChip = "Auto-Sync ON",

    routingModeSectionTitle = "Dual Output Mode",
    modeWiredBt = "3.5mm Wired + Bluetooth",
    modeWiredBtDesc = "Output media simultaneously to 3.5mm wired headphones/AUX and Bluetooth.",
    modeUsbBt = "USB Audio + Bluetooth",
    modeUsbBtDesc = "Output media simultaneously to a USB-C DAC/headset and Bluetooth.",
    modeAutoBt = "Auto (Wired/USB) + BT",
    modeAutoBtDesc = "Automatically picks 3.5mm wired (or USB fallback) paired with Bluetooth.",
    modeCustom = "Custom Pair",
    modeCustomDesc = "Pick any two currently connected audio outputs manually.",
    customPickerTitle = "Select 2 connected outputs:",

    enableDualAudioBtn = "Enable Dual Audio",
    disableDualAudioBtn = "Disable (Normal)",
    testSoundBtn = "Play Stereo Test Chime",
    stopTestSoundBtn = "Stop Test Chime",
    advancedOptionsTitle = "Advanced Options",
    autoReapplyTitle = "Background Auto-Reapply",
    autoReapplyDesc = "Automatically restores dual audio after Bluetooth or headphones reconnect.",
    primaryOrderTitle = "Primary Output Priority",
    primaryOrderWiredFirst = "Wired / USB First",
    primaryOrderBtFirst = "Bluetooth First",
    primaryOrderDesc = "Swapping primary order helps prevent audio glitches on certain devices.",

    latencyTelemetryTitle = "Live Latency Measurement",
    latencyTelemetrySubtitle = "Measures the timing gap between wired headphones and Bluetooth in milliseconds.",
    metricWiredLabel = "3.5mm Wired",
    metricBluetoothLabel = "Bluetooth",
    metricDeltaLabel = "Offset (Δ)",
    measureLatencyBtn = "Measure Latency",
    measureAndAutoSyncBtn = "Measure & Auto-Sync (<1s)",
    applyMeasuredDeltaBtn = "Apply Δ Now",
    autoSyncCardTitle = "Instant Auto-Sync Delay (< 1 sec)",
    autoSyncCardDesc = "Automatically measures and applies the exact Bluetooth/wired offset in under a second whenever headphones connect or Dual Audio starts.",
    autoSyncSpeedPrefix = "Last auto-sync in",
    delayControlTitle = "Millisecond Delay Calibration",
    delayControlExplain = "Bluetooth naturally trails by 150–300 ms. Auto-Sync sets this automatically, and you can fine-tune it anytime.",
    calibratorTitle = "Acoustic Sync Click Calibrator",
    calibratorDesc = "Wear one wired earbud and one Bluetooth earbud, start the pulse, and slide the ms value until both clicks merge into one beat.",
    startCalibratorBtn = "Start Sync Click Calibrator",
    stopCalibratorBtn = "Stop Sync Click",

    volumeMixerTitle = "Independent Volume Control",
    volumeMixerSubtitle = "Each slider controls only its own headphone without affecting the other",
    volumeIndependentBadge = "Separate",
    volumeWiredChannelLabel = "Wired 3.5mm / USB",
    volumeBluetoothChannelLabel = "Bluetooth Headset",
    volumeStepDownCd = "Decrease volume by one step",
    volumeStepUpCd = "Increase volume by one step",
    volumeMuteCd = "Mute or unmute this headphone only",
    volumeShizukuRequiredHint = "Connect Shizuku to control each headphone's volume independently.",

    detectedOutputsTitle = "Connected Audio Outputs",
    detectedOutputsSubtitle = "Updates live as you plug in or pair headphones",
    noExternalDevicesHint = "No external headphones detected yet — plug in 3.5mm wired headphones and connect Bluetooth.",
    technicalDetailsToggle = "Technical Details (Support)",
    shellNotConnectedLog = "Service not connected yet. Start Shizuku to view diagnostic details.",
    copyLogBtn = "Copy Details",
    copiedToast = "Copied to clipboard",
    refreshDiagnosticsBtn = "Refresh List",

    guideHeroTitle = "Quick Rootless Setup Guide",
    guideHeroSubtitle = "Follow these simple steps to play audio on two headphones at once with independent volume controls.",
    step1Title = "1. Install & Start Shizuku",
    step1Body = "Enable Developer Options → Wireless Debugging on Wi-Fi, pair Shizuku, and tap Start.",
    step2Title = "2. Grant Permission",
    step2Body = "Return to the Router tab, tap Grant Permission, and select Allow all the time.",
    step3Title = "3. Connect 3.5mm Wired + Bluetooth",
    step3Body = "Plug in your 3.5mm wired headphones and connect your Bluetooth headset. Both channels will show Connected.",
    step4Title = "4. Enable & Adjust Each Volume",
    step4Body = "Tap Enable Dual Audio — latency auto-syncs in under a second, and you can adjust each headphone's volume separately.",
    tipsSectionTitle = "Important Tips",
    tipWiredTitle = "3.5mm Wired Compatibility",
    tipWiredBody = "Supports standard stereo headphones, headsets with microphones, and analog AUX cables.",
    tipLatencyTitle = "Bluetooth Delay",
    tipLatencyBody = "Bluetooth naturally buffers 160–280 ms for wireless audio. Auto-Sync measures and adjusts it automatically in under a second.",
    tipSampleRateTitle = "Best Audio Clarity",
    tipSampleRateBody = "In Developer Options → Bluetooth Audio Sample Rate, select 48 kHz to match the wired jack cleanly.",
    legalSectionTitle = "Privacy & Licenses",
    privacyPolicyBtn = "Privacy Policy",
    gplLicenseBtn = "App License (GNU GPL-3.0-or-later)",
    openSourceNoticesBtn = "Copyright & Third-Party Notices",
    closeBtn = "Close",

    catWired35mm = "3.5mm Wired",
    catUsbAudio = "USB Audio",
    catBluetooth = "Bluetooth",
    catBuiltinSpeaker = "Phone Speaker",
    catOther = "Audio Output"
)

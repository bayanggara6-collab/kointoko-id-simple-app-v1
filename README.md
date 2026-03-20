# KoinToko Android App v1.0
## WebView App — kointoko.id

---

## ✅ FITUR LENGKAP

| Fitur | Status |
|---|---|
| Splash Screen animasi | ✅ |
| WebView kointoko.id | ✅ |
| Push Notification (FCM) | ✅ |
| Download file (PDF/invoice) | ✅ |
| Upload file (foto) | ✅ |
| Swipe-to-Refresh | ✅ |
| No Internet screen + retry | ✅ |
| **Biometric / PIN Lock** | ✅ |
| **In-App Update** | ✅ |
| **Screenshot Prevention** (halaman invoice) | ✅ |
| **Deep Link** (kointoko.id/* & kointoko://) | ✅ |
| **Rate App** (setelah 3/10/25 transaksi) | ✅ |
| **Dark Mode Sync** | ✅ |
| **Haptic Feedback** | ✅ |
| **Launcher Shortcuts** (Top Up, Cek Transaksi, Dashboard) | ✅ |
| Double back-press to exit | ✅ |
| Network status banner | ✅ |
| External links → browser | ✅ |
| WhatsApp CS link support | ✅ |

---

## 🚀 CARA BUILD (4 LANGKAH)

### Langkah 1 — Install Android Studio
Download: https://developer.android.com/studio

### Langkah 2 — Setup Firebase (Push Notification)
1. Buka https://console.firebase.google.com
2. Buat project → nama: KoinToko
3. Add App → Android → package: `id.kointoko.app`
4. Download **google-services.json**
5. **Timpa** file `app/google-services.json` yang ada

### Langkah 3 — Buka Project
1. Android Studio → Open → pilih folder `kointoko-android`
2. Tunggu Gradle sync (3-5 menit)

### Langkah 4 — Build AAB untuk Play Store
1. Menu: **Build → Generate Signed Bundle/APK**
2. Pilih **Android App Bundle**
3. Buat keystore baru (simpan password!)
4. Pilih **release** → Finish
5. Upload `app/release/app-release.aab` ke Play Store

---

## 🎨 GANTI ICON

Gunakan: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html

Replace file di folder `mipmap-*`:
- mdpi → 48x48px
- hdpi → 72x72px  
- xhdpi → 96x96px
- xxhdpi → 144x144px
- xxxhdpi → 192x192px

---

## 🔒 BIOMETRIC LOCK

Untuk mengaktifkan lock otomatis saat buka app:
Di `KoinTokoApp.java`, ubah default value:
```java
return getPrefs().getBoolean(KEY_BIOMETRIC_ENABLED, true); // true = aktif
```

Atau tambahkan toggle di halaman settings website yang memanggil:
```js
// Dari website JS ke Android
Android.setBiometric(true/false);
```

---

## 🔔 PUSH NOTIFICATION DARI SERVER PHP

```php
function sendPushNotification($token, $title, $body, $url = null) {
    $payload = [
        'to' => $token,
        'notification' => ['title' => $title, 'body' => $body],
        'data' => ['title' => $title, 'body' => $body, 'url' => $url ?? 'https://kointoko.id'],
        'priority' => 'high',
    ];
    $ch = curl_init('https://fcm.googleapis.com/fcm/send');
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => [
            'Authorization: key=YOUR_FCM_SERVER_KEY',
            'Content-Type: application/json',
        ],
        CURLOPT_POSTFIELDS => json_encode($payload),
    ]);
    return curl_exec($ch);
}
```

Server key: Firebase Console → Project Settings → Cloud Messaging

---

## 📁 STRUKTUR FILE
```
kointoko-android/
├── app/
│   ├── google-services.json          ← GANTI dengan yang asli dari Firebase
│   ├── build.gradle
│   └── src/main/
│       ├── java/id/kointoko/app/
│       │   ├── KoinTokoApp.java       ← Application class & preferences
│       │   ├── SplashActivity.java   ← Splash screen
│       │   ├── BiometricActivity.java← Fingerprint/PIN lock
│       │   ├── MainActivity.java     ← WebView utama (semua fitur)
│       │   └── FCMService.java       ← Push notification handler
│       ├── res/
│       │   ├── layout/
│       │   │   ├── activity_splash.xml
│       │   │   ├── activity_biometric.xml
│       │   │   └── activity_main.xml
│       │   ├── xml/
│       │   │   ├── shortcuts.xml         ← Launcher shortcuts
│       │   │   ├── file_paths.xml
│       │   │   └── network_security_config.xml
│       │   └── values/
│       │       ├── strings.xml, colors.xml, themes.xml
│       └── AndroidManifest.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

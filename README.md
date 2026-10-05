# Farzandim — Mobile Applications (Android)

Bu repozitoriy **Farzandim** ("Online Jurnal") platformasining Android mobil ilovalarini o'z ichiga oladi:
1. **Farzandim O'qituvchi (`app`)**: Dars jadvali, baholash jurnali, dars mavzulari va uyga vazifalar boshqaruvi.
2. **Farzandim Ota-onalar (`parents`)**: Farzandlar baholari, davomat, kundalik daftari va maktab e'lonlari feedi.

## Texnologiyalar
- **Til**: Kotlin 2.x
- **UI Framework**: Jetpack Compose (Material 3)
- **Arxitektura**: MVVM + MVI, Clean Architecture, Offline-first
- **Lokal Baza**: Room Database (SQLite) + WorkManager sinxronizatsiya
- **Tarmoq**: Retrofit 2 + OkHttp + Gson
- **Dependency Injection**: Koin
- **Rasm yuklash**: Coil Compose
- **Xavfsizlik**: R8 Obfuscation & Resource Shrinking

## Loyiha Tuzilishi
```
f-app/
├── app/               # Farzandim O'qituvchi (Teacher App)
├── parents/           # Farzandim Ota-onalar (Parents App)
├── gradle/            # Gradle wrapper & version catalogs (libs.versions.toml)
├── build.gradle.kts   # Root build konfiguratsiyasi
├── settings.gradle.kts# Modullar sozlamasi
└── README.md
```

## O'rnatish va Ishga tushirish

1. `local.properties` faylini yarating va backend API manzilini ko'rsating:
   ```properties
   sdk.dir=/path/to/android/sdk
   API_BASE_URL=https://api.akademx.uz
   ```

2. **Debug build**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Release build (R8 & ProGuard)**:
   ```bash
   ./gradlew assembleRelease
   ```
   Chiqarilgan APK fayllar:
   - `app/build/outputs/apk/release/app-release.apk`
   - `parents/build/outputs/apk/release/parents-release.apk`

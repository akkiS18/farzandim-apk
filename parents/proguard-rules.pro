# Add project specific ProGuard rules here for Farzandim Parents App

# --- Retrofit 2 & OkHttp ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Gson & Data Models ---
-keepattributes *Annotation*
-keepclassmembers enum * { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class uz.farzandim.parent.data.model.** { *; }
-keep class uz.farzandim.parent.data.local.entity.** { *; }

# --- Room Database ---
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomOpenHelper

# --- Koin Dependency Injection ---
-keep class * extends org.koin.core.module.Module { *; }
-dontwarn org.koin.**

# --- Coroutines ---
-dontwarn kotlinx.coroutines.**

# --- Biometric ---
-dontwarn androidx.biometric.**

# --- WorkManager & Background Sync ---
-dontwarn androidx.work.**
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class uz.farzandim.parent.data.sync.** { *; }

# --- Coil Image Loading ---
-dontwarn coil.**
-keep class coil.** { *; }

# --- Jetpack Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**


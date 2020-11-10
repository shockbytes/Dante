# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Contains rules for
# - Picasso
# - Okhttp
# - Glide
# - Retrofit
# - Realm
# - Gson
# - Jackson
# - GMS
# - Crashlytics
# - Timber
# - Firebase
# - SpeedDialView

# Activity Transition
-keep public class android.app.ActivityTransitionCoordinator

# Do not obfuscate backup models
-keepclassmembers class at.shockbytes.dante.backup.model.* {
  *;
}

-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Picasso
-dontwarn com.squareup.okhttp.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn javax.**
-dontwarn io.realm.**

# Retrofit
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions

# Gson
-keepclassmembers enum * { *; }

# gms
-keep class com.google.android.gms.* { *; }
-dontwarn com.google.android.gms.**

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.* { *; }
-dontwarn com.crashlytics.**

# Timber
-dontwarn org.jetbrains.annotations.**
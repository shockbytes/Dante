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
# - Jackson
# - Firebase

# Activity Transition
-keep public class android.app.ActivityTransitionCoordinator

# Do not obfuscate backup models
-keepclassmembers class at.shockbytes.dante.backup.model.* {
    <fields>;
    <init>();
    <methods>;
}
-keep class at.shockbytes.dante.core.book.** {*;}
-keepclassmembers class at.shockbytes.dante.core.book.* {
    <fields>;
    <init>();
    <methods>;
}

# Suggestions feature
-keep class at.shockbytes.dante.suggestions.** {*;}
-keepclassmembers class at.shockbytes.dante.suggestion.* {
    <fields>;
    <init>();
    <methods>;
}

# Seasonal theme data
-keep class at.shockbytes.dante.theme.data.** {*;}
-keepclassmembers class at.shockbytes.dante.theme.data.* {
    <fields>;
    <init>();
    <methods>;
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
 # Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
 -keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# https://github.com/square/okhttp/blob/339732e3a1b78be5d792860109047f68a011b5eb/okhttp/src/jvmMain/resources/META-INF/proguard/okhttp3.pro#L11-L14
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# If a class is used in some way by the application, and has fields annotated with @SerializedName
# and a no-args constructor, keep those fields and the constructor
# Based on https://issuetracker.google.com/issues/150189783#comment11
# See also https://github.com/google/gson/pull/2420#discussion_r1241813541 for a more detailed explanation
-if class *
-keepclasseswithmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
  @com.google.gson.annotations.SerializedName <fields>;
}

# Jackson
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

# Gson
-keepclassmembers enum * { *; }
-keep class com.google.** { *;}

# gms
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Timber
-dontwarn org.jetbrains.annotations.**
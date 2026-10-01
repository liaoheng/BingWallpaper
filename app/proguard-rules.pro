# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in d:\Programs\sdk\android/tools/proguard/proguard-android.txt
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
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Gson models: field names must survive R8 or JSON keys stop matching.
-keep class me.liaoheng.wallpaper.model.** { <fields>; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Parcelables read/written by name through their CREATOR.
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Retrofit
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# RxJava3
-dontwarn java.util.concurrent.Flow*

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-dontwarn com.google.android.gms.ads.identifier.AdvertisingIdClient$Info
-dontwarn com.google.android.gms.ads.identifier.AdvertisingIdClient
-dontwarn com.orhanobut.logger.AndroidLogAdapter
-dontwarn com.orhanobut.logger.FormatStrategy
-dontwarn com.orhanobut.logger.LogAdapter
-dontwarn com.orhanobut.logger.Logger
-dontwarn com.orhanobut.logger.PrettyFormatStrategy$Builder
-dontwarn com.orhanobut.logger.PrettyFormatStrategy

# Sentry ships its own consumer rules; keep readable traces. Mapping upload needs
# the sentry gradle plugin pointed at an auth token, otherwise release stack
# traces come back obfuscated.
-keepattributes SourceFile,LineNumberTable
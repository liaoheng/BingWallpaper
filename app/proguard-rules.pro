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
-keep class me.liaoheng.wallpaper.model.** { <fields>; }

-dontwarn com.google.android.gms.ads.identifier.AdvertisingIdClient$Info
-dontwarn com.google.android.gms.ads.identifier.AdvertisingIdClient
-dontwarn com.orhanobut.logger.AndroidLogAdapter
-dontwarn com.orhanobut.logger.FormatStrategy
-dontwarn com.orhanobut.logger.LogAdapter
-dontwarn com.orhanobut.logger.Logger
-dontwarn com.orhanobut.logger.PrettyFormatStrategy$Builder
-dontwarn com.orhanobut.logger.PrettyFormatStrategy
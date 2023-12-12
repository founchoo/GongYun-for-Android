# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 这部分内容大部分通用
-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-useuniqueclassmembernames
-dontskipnonpubliclibraryclasses
-allowaccessmodification
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes *Annotation*,InnerClasses
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**

# 这部分内容包括序列化，Android四大组件等基本内容的混淆keep
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {*;}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Jetpack Compose
-keep,includedescriptorclasses class androidx.compose.** { *; }

# Vico
-keep,includedescriptorclasses class com.patrykandpatrick.vico.** { *; }

# Hilt
-keep,includedescriptorclasses class com.google.dagger.hilt.** { *; }
-keep class * extends dagger.hilt.EntryPoint
-keep @dagger.hilt.EntryPoint public class * { public protected *; }

# Retrofit
-keep class retrofit2.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-keep class com.google.gson.** { *; }
-keep class org.jsoup.** { *; }

# OkHttp
-keep,includedescriptorclasses class com.squareup.okhttp3.** { *; }

# Gson
-keep,includedescriptorclasses class com.google.gson.** { *; }

# DataStore
-keep,includedescriptorclasses class androidx.datastore.** { *; }

# WorkManager
-keep,includedescriptorclasses class androidx.work.** { *; }

# Lifecycle
-keep,includedescriptorclasses class androidx.lifecycle.** { *; }

# Navigation
-keep,includedescriptorclasses class androidx.navigation.** { *; }

# Material
-keep,includedescriptorclasses class com.google.android.material.** { *; }

# AppCompat
-keep,includedescriptorclasses class androidx.appcompat.** { *; }

# ConstraintLayout
-keep,includedescriptorclasses class androidx.constraintlayout.** { *; }

# Legacy Support
-keep,includedescriptorclasses class androidx.legacy.** { *; }

# Apache Commons
-keep,includedescriptorclasses class org.apache.commons.** { *; }

# Browser
-keep,includedescriptorclasses class androidx.browser.** { *; }

# Glance
-keep,includedescriptorclasses class androidx.glance.appwidget.** { *; }

# Espresso
-keep,includedescriptorclasses class androidx.test.espresso.** { *; }

-dontwarn javax.script.ScriptEngine
-dontwarn javax.script.ScriptEngineManager

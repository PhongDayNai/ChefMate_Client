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

# Keep annotations/signatures for Gson generic parsing.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# Gson uses reflection; obfuscating field names in API models can break mapping.
-keep class com.watb.chefmate.data.** { *; }

# Keep entities that may be read by reflection/serialization.
-keep class com.watb.chefmate.database.entities.** { *; }

# Keep generic type tokens used by Gson.
-keep class * extends com.google.gson.reflect.TypeToken { *; }

# Keep enum helper methods.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

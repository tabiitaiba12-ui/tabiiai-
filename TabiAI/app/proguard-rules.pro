# Retrofit / OkHttp / Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.tabi.ai.data.remote.models.** { *; }
-keep class com.tabi.ai.data.remote.weather.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**

# Room
-keep class com.tabi.ai.data.local.** { *; }

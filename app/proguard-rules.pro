# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.train4send.data.** { *; }
-keep class com.train4send.Train4SendApp { *; }
-keep class com.train4send.MainActivity { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

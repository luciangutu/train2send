# Add project specific ProGuard rules here.
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod

# Keep data models and serialization logic
-keep class com.train4send.data.** { *; }
-keepclassmembers class com.train4send.data.** {
    *** Companion;
    *** $serializer;
}

# Keep critical app entry points
-keep class com.train4send.Train4SendApp { *; }
-keep class com.train4send.MainActivity { *; }

# Lifecycle & ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keep class androidx.lifecycle.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# Room generated code
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Entity { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep class * implements androidx.room.RoomDatabase$* { *; }
-keep class **.*_Impl { *; }


-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keep class se.zinokader.service.** {*;}
-keep class com.spotify.sdk.android.** {*;}
-keep class kaaes.** {*;}
-keep class com.wnafee.vector.** {*;}
-keep class retrofit.** { *; }
-keep class br.com.mauker.MsvAuthority
-keep class com.yalantis.ucrop** { *; }
-keep class rx.**
-keep class okio.**
-keep class com.squareup.okhttp.*
-keep class retrofit.appengine.UrlFetchClient
-keep interface com.yalantis.ucrop** { *; }
-dontwarn android.content.**
-keep class android.content.**


# RXJAVA RULES START
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
-dontwarn sun.misc.**
# RXJAVA RULES END

-keepclassmembers class br.com.mauker.** { *; }
-keepclassmembers class se.zinokader.spotiq.model.** {*;}
-keepclasseswithmembers class * {@retrofit.http.* <methods>;}

-dontwarn okio.**
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit.http.** <methods>;
}

-dontwarn com.squareup.okhttp.**

-keep class com.github.kaaes.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8apters.
-keepattributes Exceptions

-keepattributes *Annotation*
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
@retrofit.http.* <methods>; }
-keepattributes Signature

-keep class android.animation.*
-dontwarn rx.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.google.appengine.api.urlfetch.*
-dontwarn android.content.ServiceConnection
-dontwarn com.nineoldandroids.**
-dontwarn io.codetail.**
-keep class io.codetail.** { *; }
-keep class io.codetail.animation.SupportAnimator.* 
-dontwarn com.google.**
-keep class android.content.**
-dontwarn com.roughike.**
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>; 
}
-keep class retrofit.appengine.UrlFetchClient
-dontwarn javax.annotation.**


# For Spotify Android SDK
-keep class com.spotify.** { *; }

# For Dagger2
-keep @interface dagger.Component
-keepclassmembers @dagger.Component class * { *; }

# For Google Services
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

# For Firebase
-keepattributes *Annotation*
-keepattributes Signature
-keep class se.zinokader.spotiq.model.** { *; }
-keepclassmembers class se.zinokader.spotiq.model.** { *; }

#For Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#For Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

#For Nucleus
-dontwarn nucleus5.view.NucleusActionBarActivity

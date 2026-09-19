# JavaMail (com.sun.mail:android-mail) loads providers and handlers via
# reflection + META-INF service files, which R8 cannot trace. Without these,
# release builds fail to send email while debug builds work fine.
-keep class javax.mail.** { *; }
-keep class com.sun.mail.** { *; }
-keep class javax.activation.** { *; }
-keep class com.sun.activation.** { *; }
-dontwarn java.awt.**
-dontwarn javax.mail.**
-dontwarn com.sun.mail.**

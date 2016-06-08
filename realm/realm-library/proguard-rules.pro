-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class * { *; }
-dontwarn javax.**
-dontwarn io.realm.**
-keep class io.realm.RealmCollection
-keep class io.realm.OrderedRealmCollection
-keepclasseswithmembernames class io.realm.internal.** {
    native <methods>;
}

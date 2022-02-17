-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep @interface io.realm.annotations.RealmModule { *; }
-keep class io.realm.annotations.RealmModule { *; }

-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class * { *; }

-keep class io.realm.internal.KeepMember
-keep @io.realm.internal.KeepMember class * { @io.realm.internal.KeepMember *; }

-dontwarn javax.**
-dontwarn io.realm.**
-dontwarn io.reactivex.android.**

-keep class io.realm.RealmCollection
-keep class io.realm.OrderedRealmCollection
-keepclasseswithmembernames class io.realm.** {
    native <methods>;
}

-dontnote rx.Observable

# Referenced from JNI
-keep class org.bson.types.Decimal128 {
    public static org.bson.types.Decimal128 fromIEEE754BIDEncoding(...);
}
-keep class org.bson.types.ObjectId {
    <init>(...);
}

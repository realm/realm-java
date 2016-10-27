-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *

-keep class io.realm.internal.Keep
-keep,includedescriptorclasses @io.realm.internal.Keep class * { *; }

-keep class io.realm.internal.KeepMember
-keep,includedescriptorclasses @io.realm.internal.KeepMember class * { @io.realm.internal.KeepMember *; }

-dontwarn javax.**
-dontwarn io.realm.**
-keep class io.realm.RealmCollection
-keep class io.realm.OrderedRealmCollection
-keepclasseswithmembernames,includedescriptorclasses class io.realm.** {
    native <methods>;
}

-dontnote rx.Observable
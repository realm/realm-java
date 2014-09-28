Encryption example
------------------

Encryption support is currently an **EXPERIMENTAL** feature and requires that you build Realm from source.
We’re actively working to improve its behavior & ease-of-use.

Installation:
1. Follow the normal [build instructions](https://github.com/realm/realm-java#building-realm),
   but before running `./gradlew assemble`, add the line `encryption=true` to `local.properties`.
2. After building Realm, replace the copy of `realm-<VERSION>.aar` in your project with the one
   found at `realm/build/outputs/aar/realm-<VERSION>.aar`.


The Realm file can be stored encrypted on disk by passing a 256-bit encryption key to `Realm.create()`.
This ensures that all data persisted to disk is transparently encrypted and decrypted with standard AES-256 encryption.
The same encryption key must be supplied each time a Realm instance for the file is created.


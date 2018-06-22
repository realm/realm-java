# Library Transformer

This project contains a transformer that removes all classes, methods and fields annotated by a given annotation.

This is used by the Realm Library that comes in two variants: The normal build and a sync enabled build.
In order to get a nicer API we want to be able to inject some methods into the smsome cases w

This is used by the Realm Library as a way to emulate extension methods as we know it from Kotlin

## Known limitations

* If all constructors are stripped by this transformer, new default constructor will not be constructed resulting in invalid byte code.
* Only removing inner classes, enums and interfaces is not supported.
* If the top-level class is removed, all inner classes, enums and interfaces must also be annotated, otherwise they are not removed.
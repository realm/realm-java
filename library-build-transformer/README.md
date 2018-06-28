# Library Transformer

This project contains a transformer that removes all classes, methods and fields annotated with a given annotation.

This can be used to emulate Kotlin extension methods in cases where separating the code into flavour folders is not
feasible, like e.g. when the `Realm` class is shared between the `base` and `objectServer` flavour.

## Usage

Register the transformer as normal and provide it with the flavor to strip and annotation to detect

```
build.gradle

buildsc


import io.realm.buildtransformer.RealmBuildTransformer
android.registerTransform(new RealmBuildTransformer("base", Ob"io.realm.internal.annotations.ObjectServer"))
```

## Warning

There are no checks in place with regard to it being safe or not to remove classes and methods, so only apply the
transformer when it is safe to do so (i.e. the classes/methods/fields are not in use). Any errors will only be caught at
runtime when the actual code is accessed.

## Known limitations

* If all constructors are stripped by this transformer, a new default constructor will not be created. This will result
  in invalid byte code being generated.

* Removing inner classes, enums and interfaces but keeping the top level class is not supported.

* If the top-level class is removed, all inner classes, enums and interfaces must also be annotated, otherwise they are
  not removed, resulting in valid bytecode being generated.

* Annotations on super classes will also remove subclasses, but only the first level of inheritance.
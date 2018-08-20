# Library Transformer

This project contains a transformer that removes all classes, methods and fields annotated with a
given annotation.

This can be used to emulate Kotlin extension methods in cases where separating the code into flavour
folders is not feasible, like e.g. when the `Realm` class is shared between the `base` and
`objectServer` flavour.

## Usage

Register the transformer as normal and provide it with the flavor to strip and annotation to detect

```
import io.realm.buildtransformer.RealmBuildTransformer
android.registerTransform(new RealmBuildTransformer("base", "io.realm.internal.annotations.ObjectServer", [
  "explicit_files_to_remove"
]))
```

It is also possible to provide a specific list of files that will be removed whether or not they
have the annotation. This is used to remove some files created by the annotation processor that do
not carry over annotations.

## Warning

There are no checks in place with regard to it being safe or not to remove classes and methods, so
only apply the transformer when it is safe to do so (i.e. the classes/methods/fields are not in use).
Any errors will only be caught at runtime when the actual code is accessed.

## Known limitations

* If all constructors are stripped by this transformer, a new default constructor will not be
  created. This will result in invalid byte code being generated.

* If the top-level class is removed, all inner classes, enums and interfaces must also be annotated,
  otherwise they are not removed, resulting in valid bytecode being generated.

* Annotations on super classes will also remove subclasses, but only the first level of inheritance.

* Single enum values cannot be stripped, only the entire enum class.
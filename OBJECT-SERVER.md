# Using Realm Object Server on Android


1) Add the following to the top-level `build.gradle` file:


```groovy
buildscript {
repositories {
jcenter()
}
dependencies {
classpath "io.realm:realm-gradle-plugin:2.0.0"
}
}
```


2) Add the following to the app folders `build.gradle` file

```groovy
apply plugin: 'realm-android'

realm {
syncEnabled = true
}
```

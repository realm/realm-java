![Realm](logo.png)

Realm is a mobile database that runs directly inside phones, tablets or wearables.
This repository holds the source code for the Java version of Realm, which currently runs only on Android.

## Features

* **Mobile-first:** Realm is the first database built from the ground up to run directly inside phones, tablets and wearables.
* **Simple:** Data is directly exposed as objects and queryable by code, removing the need for ORM's riddled with performance & maintenance issues. Plus, we've worked hard to [keep our API down to very few classes](http://realm.io/docs/java/): most of our users pick it up intuitively, getting simple apps up & running in minutes.
* **Modern:** Realm supports easy thread-safety, relationships & encryption.
* **Fast:** Realm is faster than even raw SQLite on common operations, while maintaining an extremely rich feature set.

## Getting Started

Please see the [detailed instructions in our docs](http://realm.io/docs/java/#installation) to add Realm to your project.

## Documentation

Documentation for Realm can be found at [realm.io/docs/java](http://realm.io/docs/java).
The API reference is located at [realm.io/docs/java/api](http://realm.io/docs/java/api).

## Getting Help

- **Need help with your code?**: Look for previous questions on the [#realm tag](https://stackoverflow.com/questions/tagged/realm?sort=newest) — or [ask a new question](http://stackoverflow.com/questions/ask?tags=realm). We activtely monitor & answer questions on SO!
- **Have a bug to report?** [Open an issue](https://github.com/realm/realm-java/issues/new). If possible, include the version of Realm, a full log, the Realm file, and a project that shows the issue.
- **Have a feature request?** [Open an issue](https://github.com/realm/realm-java/issues/new). Tell us what the feature should do, and why you want the feature.
- Sign up for our [**Community Newsletter**](http://eepurl.com/VEKCn) to get regular tips, learn about other use-cases and get alerted of blogposts and tutorials about Realm.

## Using Snapshots

If you want to test recent bugfixes or features that have not been packaged in an official release yet, you can use a **-SNAPSHOT** release of the current development version of Realm via Gradle, available on [OJO](http://oss.jfrog.org/oss-snapshot-local/io/realm/realm-android/)

```gradle
buildscript {
    repositories {
        maven {
            url 'http://oss.jfrog.org/artifactory/oss-snapshot-local'
        }
    }
    dependencies {
        classpath "io.realm:realm-gradle-plugin:<version>-SNAPSHOT"
    }
}

repositories {
    maven {
        url 'http://oss.jfrog.org/artifactory/oss-snapshot-local'
    }
}
```

See [version.txt](version.txt) for the latest version number.

## Building Realm

In case you don't want to use the precompiled version, you can build Realm yourself from source.

Prerequisites:

 * Make sure `make` is available in your `$PATH`
 * Download the [**JDK 7**](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or [**JDK 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) from Oracle and install it.
 * Download & install the Android SDK, **Android 4.4.2 (API 19)**, **Android 4.4W (API 20)** and **Android 5.0 (API 21)** (for example through Android Studio’s **Android SDK Manager**)
 * Download the **Android NDK (= r10e)** for [OS X](http://dl.google.com/android/ndk/android-ndk-r10e-darwin-x86_64.bin) or [Linux](http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin).
 * Or you can use [Hombrew](https://github.com/Homebrew/homebrew) to install Android NDK for Mac:

    ```
    brew install android-ndk
    ```

 * Add two environment variables to your profile:

    ```
    export ANDROID_HOME=~/Library/Android/sdk
    export NDK_HOME=/usr/local/Cellar/android-ndk/r10e
    ```

 * If you are using OS X, you'd be better to add following lines to `~/.profile` in order for Android Studio to see those environment variables.

    ```
    launchctl setenv ANDROID_HOME "$ANDROID_HOME"
    launchctl setenv NDK_HOME "$NDK_HOME"
    ```

Once you have completed all the pre-requisites building Realm is done with a simple command

```
./gradlew assemble
```

That command will generate:

 * a jar file for the Realm Gradle plugin
 * an aar file for the Realm library
 * a jar file for the annotations
 * a jar file for the annotations processor

### Other Commands

 * `./gradlew tasks` will show all the available tasks
 * `./gradlew javadoc` will generate the Javadocs
 * `./gradlew monkeyExamples` will run the monkey tests on all the examples
 * `./gradlew installRealmJava` will install the Realm library and plugin to mavenLocal()
 * `./gradlew clean -PdontCleanJniFiles` will remove all generated files except for JNI related files. This saves recompilation time a lot.

Generating the Javadoc using the command above will report a large number of warnings. The Javadoc is generated, and we will fix the issue in the near future.

### Gotchas

The repository is organized in four Gradle projects:

 * `realm`: it contains the actual library (including the JNI layer), the annotations and the annotations processor.
 * `gradle-plugin`: it contains the Gradle plugin.
 * `examples`: it contains the example projects. This project directly depends on `gradle-plugin` which adds a dependency to the artifacts produced by `realm`.
 * The root folder is another Gradle project and all it does is orchestrating the other jobs

This means that `./gradlew clean` and `./gradlew cleanExamples` will fail if `assembleExamples` has not been executed first.
Note that IntelliJ [does not support multiple projects in the same window](https://youtrack.jetbrains.com/issue/IDEABKL-6118#)
so each sub-project must be opened in its own window.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details!

## License

Realm Java is published under the Apache 2.0 license.
The underlying core is available under the [Realm Core Binary License](LICENSE#L210-L243) while we [work to open-source it under the Apache 2.0 license](http://realm.io/docs/java/#faq).

**This product is not being made available to any person located in Cuba, Iran,
North Korea, Sudan, Syria or the Crimea region, or to any other person that is
not eligible to receive the product under U.S. law.**

## Feedback

**_If you use Realm and are happy with it, all we ask is that you please consider sending out a tweet mentioning [@realm](http://twitter.com/realm), announce your app on [our mailing-list](https://groups.google.com/forum/#!forum/realm-java), or email [help@realm.io](mailto:help@realm.io) to let us know about it!_**

**_And if you don't like it, please let us know what you would like improved, so we can fix it!_**

![analytics](https://ga-beacon.appspot.com/UA-50247013-2/realm-java/README?pixel)

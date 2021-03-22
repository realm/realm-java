![Realm](logo.png)

[![bintray](https://api.bintray.com/packages/realm/maven/realm-gradle-plugin/images/download.svg) ](https://bintray.com/realm/maven/realm-gradle-plugin/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache-blue.svg)](https://github.com/realm/realm-java/blob/master/LICENSE)

Realm is a mobile database that runs directly inside phones, tablets or wearables.
This repository holds the source code for the Java version of Realm, which currently runs only on Android.

## Realm Kotlin

See [Realm Kotlin](https://github.com/realm/realm-kotlin) for more information about our new SDK written specifically for Kotlin Multiplatform and Android. The SDK is still experimental and the API surface has not been finalized yet, but we highly encourage any feedback you might have.

## Features

* **Mobile-first:** Realm is the first database built from the ground up to run directly inside phones, tablets, and wearables.
* **Simple:** Data is directly exposed as objects and queryable by code, removing the need for ORM's riddled with performance & maintenance issues. Plus, we've worked hard to [keep our API down to very few classes](https://realm.io/docs/java/): most of our users pick it up intuitively, getting simple apps up & running in minutes.
* **Modern:** Realm supports easy thread-safety, relationships & encryption.
* **Fast:** Realm is faster than even raw SQLite on common operations while maintaining an extremely rich feature set.

## Getting Started

Please see the [detailed instructions in our docs](https://realm.io/docs/java/latest/#installation) to add Realm to your project.

## Documentation

Documentation for Realm can be found at [realm.io/docs/java](https://realm.io/docs/java).
The API reference is located at [realm.io/docs/java/api](https://realm.io/docs/java/api).

## Getting Help

- **Got a question?**: Look for previous questions on the [#realm tag](https://stackoverflow.com/questions/tagged/realm?sort=newest) — or [ask a new question](http://stackoverflow.com/questions/ask?tags=realm). We actively monitor & answer questions on StackOverflow! You can also check out our [Community Forum](https://developer.mongodb.com/community/forums/tags/c/realm/9/realm-sdk) where general questions about how to do something can be discussed.
- **Think you found a bug?** [Open an issue](https://github.com/realm/realm-java/issues/new?template=bug_report.md). If possible, include the version of Realm, a full log, the Realm file, and a project that shows the issue.
- **Have a feature request?** [Open an issue](https://github.com/realm/realm-java/issues/new?template=feature_request.md). Tell us what the feature should do, and why you want the feature.

## Using Snapshots

If you want to test recent bugfixes or features that have not been packaged in an official release yet, you can use a **-SNAPSHOT** release of the current development version of Realm via Gradle, available on [JFrog OSS](http://oss.jfrog.org/oss-snapshot-local/io/realm/realm-gradle-plugin/)

```
buildscript {
    repositories {
        mavenCentral()
        google()
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
        jcenter()
    }
    dependencies {
        classpath "io.realm:realm-gradle-plugin:<version>-SNAPSHOT"
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
        jcenter()
    }
}
```

See [version.txt](version.txt) for the latest version number.

## Building Realm

In case you don't want to use the precompiled version, you can build Realm yourself from source.

### Prerequisites

 * Download the [**JDK 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) from Oracle and install it.
 * The latest stable version of Android Studio. Currently [4.1.1](https://developer.android.com/studio/).
 * Download & install the Android SDK **Build-Tools 29.0.3**, **Android Pie (API 29)** (for example through Android Studio’s **Android SDK Manager**).
 * Install CMake version 3.18.4 and build Ninja.
 * Install the NDK (Side-by-side) **21.0.6113669** from the SDK Manager in Android Studio. Remember to check `☑  Show package details` in the manager to display all available versions.

 * Add the Android home environment variable to your profile:

    ```
    export ANDROID_HOME=~/Library/Android/sdk
    ```

 * If you are launching Android Studio from the macOS Finder, you should also run the following command:

    ```
    launchctl setenv ANDROID_HOME "$ANDROID_HOME"
    ```

 * If you'd like to specify the location in which to store the archives of Realm Core, define the `REALM_CORE_DOWNLOAD_DIR` environment variable. It enables caching core release artifacts.

   ```
   export REALM_CORE_DOWNLOAD_DIR=~/.realmCore
   ```

   macOS users must also run the following command for Android Studio to see this environment variable.

   ```
   launchctl setenv REALM_CORE_DOWNLOAD_DIR "$REALM_CORE_DOWNLOAD_DIR"
   ```

It would be a good idea to add all of the symbol definitions (and their accompanying `launchctl` commands, if you are using macOS) to your `~/.profile` (or `~/.zprofile` if the login shell is `zsh`)

 * If you develop Realm Java with Android Studio, we recommend you to exclude some directories from indexing target by executing following steps on Android Studio. It really speeds up indexing phase after the build.

    - Under `/realm/realm-library/`, select `build`, `.cxx` and `distribution` folders in `Project` view.
    - Press `Command + Shift + A` to open `Find action` dialog. If you are not using default keymap nor using macOS, you can find your shortcut key in `Keymap` preference by searching `Find action`.
    - Search `Excluded` (not `Exclude`) action and select it. Selected folder icons should become orange (in default theme).
    - Restart Android Studio.

### Download sources

You can download the source code of Realm Java by using git. Since realm-java has git submodules, use `--recursive` when cloning the repository.

```
git clone git@github.com:realm/realm-java.git --recursive
```

or

```
git clone https://github.com/realm/realm-java.git --recursive
```

### Build

Once you have completed all the pre-requisites building Realm is done with a simple command.

```
./gradlew assemble
```

That command will generate:

 * a jar file for the Realm Gradle plugin
 * an aar file for the Realm library
 * a jar file for the annotations
 * a jar file for the annotations processor

The full build may take an hour or more, to complete.

### Building from source

It is possible to build Realm Java with the submodule version of Realm Core. This is done by providing the following parameter when building: `-PbuildCore=true`.

```
./gradlew assembleBase -PbuildCore=true
```

You can turn off interprocedural optimizations with the following parameter: `-PenableLTO=false`. 

```
./gradlew assembleBase -PenableLTO=false`
```

Note: Building the `Base` variant would always build realm-core.

Note: Interprocedural optimizations are enabled by default.

Note: If you want to build from source inside Android Studio, you need to update the Gradle parameters by going into the Realm projects settings `Settings > Build, Execution, Deployment > Compiler > Command-line options` and add `-PbuildCore=true` or `-PenableLTO=false` to it. Alternatively you can add it into your `gradle.properties`:

```
buildCore=true
enableLTO=false
```

Note: If building on OSX you might like to prevent Gatekeeper to block all NDK executables by disabling it: `sudo spctl --master-disable`. Remember to enable it afterwards: `sudo spctl --master-enable`

### Other Commands

 * `./gradlew tasks` will show all the available tasks
 * `./gradlew javadoc` will generate the Javadocs
 * `./gradlew monkeyExamples` will run the monkey tests on all the examples
 * `./gradlew installRealmJava` will install the Realm library and plugin to mavenLocal()
 * `./gradlew clean -PdontCleanJniFiles` will remove all generated files except for JNI related files. This reduces recompilation time a lot.
 * `./gradlew connectedUnitTests -PbuildTargetABIs=$(adb shell getprop ro.product.cpu.abi)` will build JNI files only for the ABI which corresponds to the connected device.  These tests require a running Object Server (see below)

Generating the Javadoc using the command above may generate warnings. The Javadoc is generated despite the warnings.


### Upgrading Gradle Wrappers

 All gradle projects in this repository have `wrapper` task to generate Gradle Wrappers. Those tasks refer to `gradle` property defined in `/dependencies.list` to determine Gradle Version of generating wrappers.
We have a script `./tools/update_gradle_wrapper.sh` to automate these steps. When you update Gradle Wrappers, please obey the following steps.

 1. Edit `gradle` property in defined in `/dependencies.list` to new Gradle Wrapper version.
 2. Execute `/tools/update_gradle_wrapper.sh`.

### Gotchas

The repository is organized into six Gradle projects:

 * `realm`: it contains the actual library (including the JNI layer) and the annotations processor.
 * `realm-annotations`: it contains the annotations defined by Realm.
 * `realm-transformer`: it contains the bytecode transformer.
 * `gradle-plugin`: it contains the Gradle plugin.
 * `examples`: it contains the example projects. This project directly depends on `gradle-plugin` which adds a dependency to the artifacts produced by `realm`.
 * The root folder is another Gradle project.  All it does is orchestrate the other jobs.

This means that `./gradlew clean` and `./gradlew cleanExamples` will fail if `assembleExamples` has not been executed first.
Note that IntelliJ [does not support multiple projects in the same window](https://youtrack.jetbrains.com/issue/IDEABKL-6118#)
so each of the six Gradle projects must be imported as a separate IntelliJ project.

Since the repository contains several completely independent Gradle projects, several independent builds are run to assemble it.
Seeing a line like: `:realm:realm-library:compileBaseDebugAndroidTestSources UP-TO-DATE` in the build log does *not* imply
that you can run `./gradlew :realm:realm-library:compileBaseDebugAndroidTestSources`.

## Examples

The `./examples` folder contains many example projects showing how Realm can be used. If this is the first time you checkout or pull a new version of this repository to try the examples, you must call `./gradlew installRealmJava` from the top-level directory first. Otherwise, the examples will not compile as they depend on all Realm artifacts being installed in `mavenLocal()`.

Standalone examples can be [downloaded from website](https://realm.io/docs/java/latest/#getting-started).

## Running Tests on a Device

To run these tests, you must have a device connected to the build computer, and the `adb` command must be in your `PATH`

1. Connect an Android device and verify that the command `adb devices` shows a connected device:

    ```sh
    adb devices
    List of devices attached
    004c03eb5615429f device
    ```

2. Run instrumentation tests:

    ```sh
    cd realm
    ./gradlew connectedBaseDebugAndroidTest
    ```

These tests may take as much as half an hour to complete.

## Running Tests Using The Realm Object Server

Tests in `realm/realm-library/src/syncIntegrationTest` require a running testing server to work.
A docker image can be built from `tools/sync_test_server/Dockerfile` to run the test server.
`tools/sync_test_server/start_server.sh` will build the docker image automatically.

To run a testing server locally:

1. Install [docker](https://www.docker.com/products/overview) and run it.

2. Run `tools/sync_test_server/start_server.sh`:

    ```sh
    cd tools/sync_test_server
    ./start_server.sh
    ```

    This command will not complete until the server has stopped.

3. Run instrumentation tests

    In a new terminal window, run:

    ```sh
    cd realm
    ./gradlew connectedObjectServerDebugAndroidTest
    ```

Note that if using VirtualBox (Genymotion), the network needs to be bridged for the tests to work.
This is done in `VirtualBox > Network`. Set "Adapter 2" to "Bridged Adapter".

These tests may take as much as half an hour to complete.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details!

This project adheres to the [Contributor Covenant Code of Conduct](https://realm.io/conduct).
By participating, you are expected to uphold this code. Please report
unacceptable behavior to [info@realm.io](mailto:info@realm.io).

The directory `realm/config/studio` contains lint and style files recommended for project code.
Import them from Android Studio with Android Studio > Preferences... > Code Style > Manage... > Import,
or Android Studio > Preferences... > Inspections > Manage... > Import.  Once imported select the
style/lint in the drop-down to the left of the Manage... button.

## License

Realm Java is published under the Apache 2.0 license.

Realm Core is also published under the Apache 2.0 license and is available
[here](https://github.com/realm/realm-core).

**This product is not being made available to any person located in Cuba, Iran,
North Korea, Sudan, Syria or the Crimea region, or to any other person that is
not eligible to receive the product under U.S. law.**

## Feedback

**_If you use Realm and are happy with it, all we ask is that you, please consider sending out a tweet mentioning [@realm](http://twitter.com/realm) to share your thoughts!_**

**_And if you don't like it, please let us know what you would like improved, so we can fix it!_**

![analytics](https://ga-beacon.appspot.com/UA-50247013-2/realm-java/README?pixel)

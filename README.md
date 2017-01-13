![Realm](logo.png)

Realm is a mobile database that runs directly inside phones, tablets or wearables.
This repository holds the source code for the Java version of Realm, which currently runs only on Android.

## Features

* **Mobile-first:** Realm is the first database built from the ground up to run directly inside phones, tablets and wearables.
* **Simple:** Data is directly exposed as objects and queryable by code, removing the need for ORM's riddled with performance & maintenance issues. Plus, we've worked hard to [keep our API down to very few classes](https://realm.io/docs/java/): most of our users pick it up intuitively, getting simple apps up & running in minutes.
* **Modern:** Realm supports easy thread-safety, relationships & encryption.
* **Fast:** Realm is faster than even raw SQLite on common operations, while maintaining an extremely rich feature set.

## Getting Started

Please see the [detailed instructions in our docs](https://realm.io/docs/java/#installation) to add Realm to your project.

## Documentation

Documentation for Realm can be found at [realm.io/docs/java](https://realm.io/docs/java).
The API reference is located at [realm.io/docs/java/api](https://realm.io/docs/java/api).

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

allprojects {
    repositories {
        maven {
            url 'http://oss.jfrog.org/artifactory/oss-snapshot-local'
        }
    }
}
```

See [version.txt](version.txt) for the latest version number.

## Building Realm

In case you don't want to use the precompiled version, you can build Realm yourself from source.

### Prerequisites

 * Download the [**JDK 7**](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or [**JDK 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) from Oracle and install it.
 * Download & install the Android SDK **Build-Tools 24.0.0**, **Android N (API 24)** (for example through Android Studio’s **Android SDK Manager**).
 * Download the **Android NDK (= r10e)** for [OS X](http://dl.google.com/android/ndk/android-ndk-r10e-darwin-x86_64.bin) or [Linux](http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin).
 * Install CMake from SDK manager in Android Studio ("SDK Tools" -> "CMake").
 * Or you can use [Hombrew-versions](https://github.com/Homebrew/homebrew-versions) to install Android NDK for Mac:

    ```
    brew tap homebrew/versions
    brew install android-ndk-r10e
    ```

 * Add two environment variables to your profile (presuming you used brew to install the NDK):

    ```
    export ANDROID_HOME=~/Library/Android/sdk
    export ANDROID_NDK_HOME=/usr/local/Cellar/android-ndk-r10e/r10e
    ```

 * If you want to build with Android Studio, `ndk.dir` has to be defined in the `realm/local.properties` as well.  Note that there is a `local.properites` in the root directory that is *not* the one that needs to be edited.  Again, presuming you used brew to install the NDK:

    ```
    ndk.dir=/usr/local/Cellar/android-ndk-r10e/r10e
    ```

 * If you will be launching Android Studio from the OS X Finder, you should also run the following two commands:

    ```
    launchctl setenv ANDROID_HOME "$ANDROID_HOME"
    launchctl setenv ANDROID_NDK_HOME "$ANDROID_NDK_HOME"
    ```

 * If you'd like to specify the location in which to store the archives of Realm Core, define the `REALM_CORE_DOWNLOAD_DIR` environment variable. It enables you to keep Core's archive when executing `git clean -xfd`.

   ```
   export REALM_CORE_DOWNLOAD_DIR=~/.realmCore
   ```

   OS X users must also run the following command in order for Android Studio to see this environment variable..

   ```
   launchctl setenv REALM_CORE_DOWNLOAD_DIR "$REALM_CORE_DOWNLOAD_DIR"
   ```

It would be a good idea to add all of the symbol definitions (and their accompanying `launchctl` commands, if you are using OS X) to your `~/.profile` (or `~/.zprofile` if the login shell is `zsh`)


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

Once you have completed all the pre-requisites building Realm is done with a simple command

```
./gradlew assemble
```

That command will generate:

 * a jar file for the Realm Gradle plugin
 * an aar file for the Realm library
 * a jar file for the annotations
 * a jar file for the annotations processor

The full build may take an hour or more, to complete.

### Other Commands

 * `./gradlew tasks` will show all the available tasks
 * `./gradlew javadoc` will generate the Javadocs
 * `./gradlew monkeyExamples` will run the monkey tests on all the examples
 * `./gradlew installRealmJava` will install the Realm library and plugin to mavenLocal()
 * `./gradlew clean -PdontCleanJniFiles` will remove all generated files except for JNI related files. This reduces recompilation time a lot.
 * `./gradlew connectedUnitTests -PbuildTargetABIs=$(adb shell getprop ro.product.cpu.abi)` will build JNI files only for the ABI which corresponds to the connected device.  These tests require a running Object Server (see below)

Generating the Javadoc using the command above may generate warnings. The Javadoc is generated despite the warnings.

### Gotchas

The repository is organized in six Gradle projects:

 * `realm`: it contains the actual library (including the JNI layer) and the annotations processor.
 * `realm-annotations`: it contains the annotations defined by Realm.
 * `realm-transformer`: it contains the bytecode transformer.
 * `gradle-plugin`: it contains the Gradle plugin.
 * `examples`: it contains the example projects. This project directly depends on `gradle-plugin` which adds a dependency to the artifacts produced by `realm`.
 * The root folder is another Gradle project.  All it does is orchestrate the other jobs

This means that `./gradlew clean` and `./gradlew cleanExamples` will fail if `assembleExamples` has not been executed first.
Note that IntelliJ [does not support multiple projects in the same window](https://youtrack.jetbrains.com/issue/IDEABKL-6118#)
so each of the six Gradle projects must be imported as a separate IntelliJ project.

Since the repository contains several completely independent Gradle projects, several independent builds are run to assemble it.
Seeing a line like: `:realm:realm-library:compileBaseDebugAndroidTestSources UP-TO-DATE` in the build log does *not* imply
that you can run `./gradlew :realm:realm-library:compileBaseDebugAndroidTestSources`.

## Examples

The `./examples` folder contain a number of example projects showing how Realm can be used. If this is the first time you checkout or pull a new version of this repository to try the examples, you must call `./gradlew installRealmJava` from the top-level directory first. Otherwise the examples will not compile as they depend on all Realm artifacts being installed in `mavenLocal()`.

Standalone examples can be [downloaded from website](https://realm.io/docs/java/latest/#getting-started).

## Running Tests on a Device

To run these tests you must have a device connected to the build computer and the `adb` command must be in your `PATH`

1. Connect an Android device and verify that that the command `adb devices` shows a connected device:

        ```sh
        adb devices
        List of devices attached
        004c03eb5615429f	device
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

1. Install [docker](https://www.docker.com/products/overview).

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
The underlying core is available under the [Realm Core Binary License](LICENSE#L210-L243) while we [work to open-source it under the Apache 2.0 license](https://realm.io/docs/java/#faq).

**This product is not being made available to any person located in Cuba, Iran,
North Korea, Sudan, Syria or the Crimea region, or to any other person that is
not eligible to receive the product under U.S. law.**

## Feedback

**_If you use Realm and are happy with it, all we ask is that you please consider sending out a tweet mentioning [@realm](http://twitter.com/realm), announce your app on [our mailing-list](https://groups.google.com/forum/#!forum/realm-java), or email [help@realm.io](mailto:help@realm.io) to let us know about it!_**

**_And if you don't like it, please let us know what you would like improved, so we can fix it!_**

![analytics](https://ga-beacon.appspot.com/UA-50247013-2/realm-java/README?pixel)

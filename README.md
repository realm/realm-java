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

- **Need help with your code?**: Look for previous questions under the tag [#realm](https://stackoverflow.com/questions/tagged/realm?sort=newest) — or [open a new one](http://stackoverflow.com/questions/ask?tags=realm). We activtely monitor & answer questions SO!
- **Have a bug to report?** [Open an issue](https://github.com/realm/realm-java/issues). If possible, include the version of Realm, a full log, the Realm file, and a project that shows the issue.
- **Have a feature request?** [Open an issue](https://github.com/realm/realm-java/issues). Tell us what the feature should do, and why you want the feature.
- **Want to ask in-depth questions?** [Join our online office hours](https://attendee.gotowebinar.com/rt/1182038037080364033). We host these once a month, and you can join via chat, audio call, or video call.
- Sign up for our [**Community Newsletter**](http://eepurl.com/VEKCn) to get regular tips, learn about other use-cases and get alerted of blogposts and tutorials about Realm.

## Using Snapshots

If you want to test recent bugfixes or features that have not been packaged in an official release yet, you can use a **-SNAPSHOT** release of the current development version of Realm via Gradle, available on [JFrog](http://oss.jfrog.org/oss-snapshot-local/io/realm/realm-android/)

    repositories {
        maven {
            url 'http://oss.jfrog.org/artifactory/oss-snapshot-local'
        }
    }

    dependencies {
      compile 'io.realm:realm-android:0.80.2-SNAPSHOT'
    }

## Building Realm

In case you don't want to use the precompiled version, you can build Realm yourself from source.

Prerequisites:

* Make sure `make` is available in your `$PATH`
* Download the [**JDK 7**](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or [**JDK 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) from Oracle and install it.
* Download & install the Android SDK, **Android 4.4W (API 20)** (for example through Android Studio’s **Android SDK Manager**)
* _Also_ download & install the Android SDK, **Android 4.4.2 (API 20)** (for example through Android Studio’s **Android SDK Manager**)
* Download the **Android NDK (= r10d)**. For example, on Mac OS you can do this with [Homebrew](http://brew.sh) with `brew install android-ndk`.
* Add a `local.properties` file at the root of this folder with the correct paths for the Android SDK and NDK, for example:

    ```
    sdk.dir=/Applications/Android Studio.app/sdk
    ndk.dir=/usr/local/Cellar/android-ndk/r10d
    ```

Once you have completed all the pre-requisites building Realm is done with a simple command

    ./gradlew androidJar

That command will generate the .jar file containing the Realm runtime and the annotation processor. You will find it in realm/build/libs.

### Other Commands

 * `./gradlew realm:javadocRelease` will generate the Javadocs
 * `./gradlew realm:connectedCheck` will run the tests on a connected Android device

Generating the Javadoc using the command above will report a failure (1 error, 30+ warnings). The Javadoc is generated, and we will fix 
`realm/build.gradle` in the near future.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details!

## License

Realm Java is published under the Apache 2.0 license.  
The underlying core is available under the [Realm Core Binary License](LICENSE#L210-L243) while we [work to open-source it under the Apache 2.0 license](http://realm.io/docs/java/#faq).

## Feedback

**_If you use Realm and are happy with it, all we ask is that you please consider sending out a tweet mentioning [@realm](http://twitter.com/realm), announce your app on [our mailing-list](https://groups.google.com/forum/#!forum/realm-java), or email [help@realm.io](mailto:help@realm.io) to let us know about it!_**

**_And if you don't like it, please let us know what you would like improved, so we can fix it!_**

![analytics](https://ga-beacon.appspot.com/UA-50247013-2/realm-java/README?pixel)

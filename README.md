![Realm](logo.png)

Realm is a mobile database that runs directly inside phones, tablets or wearables.
This repository holds the source code for the Java version of Realm, which currently runs only on Android.

## Features

* **Mobile-first:** Realm is the first database built from the ground up to run directly inside phones, tablets and wearables.
* **Simple:** Data is directly exposed as objects and queryable by code, removing the need for ORM's riddled with performance & maintenance issues. Plus, we've worked hard to [keep our API down to very few classes](http://realm.io/docs/java/): most of our users pick it up intuitively, getting simple apps up & running in minutes.
* **Modern:** Realm supports easy thread-safety, relationships & encryption.
* **Fast:** Realm is faster than even raw SQLite on common operations, while maintaining an extremely rich feature set.

## Setting up Realm in your app

See full instructions in our [docs](http://realm.io/docs/java).

## Documentation

Documentation for Realm can be found at [realm.io/docs/java](http://realm.io/docs/java). The API reference is located at [realm.io/docs/java/api](http://realm.io/docs/java/api).

## Building Realm

Prerequisites:

* Make sure `make` is available in your `$PATH`
* Download the [**JDK 7**](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or [**JDK 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) from Oracle and install it.
* Download & install the Android SDK, **Android 4.4W (API 20)** (for example through Android Studio’s **Android SDK Manager**)
* _Also_ download & install the Android SDK, **Android 4.4.2 (API 19)** (for example through Android Studio’s **Android SDK Manager**)
* Download the **Android NDK (>= r9d)**. For example, on Mac OS you can do this with [Homebrew](http://brew.sh) with `brew install android-ndk`.
* Add a `local.properties` file at the root of this folder with the correct paths for the Android SDK and NDK, for example:

    ```
    sdk.dir=/Applications/Android Studio.app/sdk
    ndk.dir=/usr/local/Cellar/android-ndk/r9d
    ```

Once you have completed all the pre-requisites building Realm is done with a simple command

    ./gradlew assemble
    
    
That command will generate the .aar file and annotation processor jar for Realm. You will find them in realm/build/outputs/aar and realm-annotations-processor/build/libs, respectively.

### Other Commands

 * `./gradlew generatereleaseJavadoc` will generate the Javadocs
 * `./gradlew connectedCheck` will run the tests on a connected Android device

Generating the Javadoc using the command above will report a failure (1 error, 30+ warnings). The Javadoc is generated, and we will fix 
`realm/build.gradle` in the near future.

## Filing Issues

Whether you find a bug, typo or an API call that could be clarified, please [file an issue](https://github.com/realm/realm-java/issues) on our GitHub repository.

When filing an issue, please provide as much of the following information as possible in order to help others fix it:

1. **Goals**
2. **Expected results**
3. **Actual results**
4. **Steps to reproduce**
5. **Code sample that highlights the issue** (link to full Android Studio projects that we can compile ourselves are ideal)
6. **Version of Realm/Android Studio/OS**

If you'd like to send us sensitive sample code to help troubleshoot your issue, you can email <help@realm.io> directly.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details!

## License

Realm Java is published under the Apache 2.0 license.  
The underlying core is available under the [Realm Core Binary License](LICENSE#L210-L243) while we [work to open-source it under the Apache 2.0 license](http://realm.io/docs/java/#faq).

## Feedback

**_If you use Realm and are happy with it, all we ask is that you please consider sending out a tweet mentioning [@realm](http://twitter.com/realm), announce your app on [our mailing-list](https://groups.google.com/forum/#!forum/realm-java), or email [info@realm.io](mailto:info@realm.io) to let us know about it!_**

**_And if you don't like it, please let us know what you would like improved, so we can fix it!_**

![analytics](https://ga-beacon.appspot.com/UA-50247013-2/realm-java/README?pixel)

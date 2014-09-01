# Realm Android

## Prerequisites

 * The Android SDK must be installed
 * The Android NDK must be installed
 * `make` must be available in `$PATH`
 * A JDK installed

## Setup

 * Modify the `local.properties` file with the correct paths for the Android SDK and NDK

## Build

 * `./gradlew assemble` will generate the `.aar` file
 * `./gradlew generatereleaseJavadoc` will generate the Javadocs
 * `./gradlew connectedCheck` will run the tests on a connected Android device

## Test

This is a perfect test

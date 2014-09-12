#!/usr/bin/env bash

echo "Building the annotation processor"
./gradlew realm-annotation-processor:assemble

echo "Building Realm"
./gradlew realm:assemble

echo "Building the Javadocs"
./gradlew javadocReleaseJar

echo "Copying files to the distribution folder"
cp realm-annotations-processor/build/libs/realm-annotations-processor-0.1.0.jar distribution
cp realm/build/outputs/aar/realm-0.1.0.aar distribution/realm
cp -R realm/build/docs/javadoc distribution
cp realm/build/libs/realm-0.1.0-javadoc.jar distribution


echo "Copying files to the distribution/RealmBasicExample folder"
mkdir -p distribution/RealmBasicExample/app/libs
cp realm-annotations-processor/build/libs/realm-annotations-processor-0.1.0.jar distribution/RealmBasicExample/app/libs
mkdir -p distribution/RealmBasicExample/realm
cp -R distribution/realm distribution/RealmBasicExample

echo "Copying files to the distribution/RealmGridViewExample folder"
mkdir -p distribution/RealmGridViewExample/app/libs
cp realm-annotations-processor/build/libs/realm-annotations-processor-0.1.0.jar distribution/RealmGridViewExample/app/libs
mkdir -p distribution/RealmGridViewExample/realm
cp -R distribution/realm distribution/RealmGridViewExample

echo  "Done"

#!/bin/bash

set -e

version=$(cat version.txt)

echo "Deleting old artifacts"
find distribution -name "realm*.jar" -delete
find distribution -name "realm*.aar" -delete

echo "Building the annotation processor"
(
    cd realm-annotations-processor
    ./gradlew assemble
)

echo "Building Realm"
./gradlew realm:assemble

echo "Building the Javadocs"
./gradlew realm:javadocReleaseJar

echo "Copying files to the distribution folder"
cp -f version.txt distribution
cp -f changelog.txt distribution
cp realm-annotations-processor/build/libs/realm-annotations-processor-${version}.jar distribution
cp realm/build/outputs/aar/realm-${version}.aar distribution/realm
cp -R realm/build/docs/javadoc distribution
cp realm/build/libs/realm-${version}-javadoc.jar distribution

echo "Copying files to the distribution/RealmIntroExample folder"
cp -R examples/introExample/src distribution/RealmIntroExample/app
mkdir -p distribution/RealmIntroExample/app/libs
cp realm-annotations-processor/build/libs/realm-annotations-processor-${version}.jar distribution/RealmIntroExample/app/libs
mkdir -p distribution/RealmIntroExample/realm
cp -R distribution/realm distribution/RealmIntroExample

echo "Copying files to the distribution/RealmGridViewExample folder"
cp -R examples/gridViewExample/src distribution/RealmGridViewExample/app
mkdir -p distribution/RealmGridViewExample/app/libs
cp realm-annotations-processor/build/libs/realm-annotations-processor-${version}.jar distribution/RealmGridViewExample/app/libs
mkdir -p distribution/RealmGridViewExample/realm
cp -R distribution/realm distribution/RealmGridViewExample

echo "Copying files to the distribution/RealmConcurrencyExample folder"
mkdir -p distribution/RealmConcurrencyExample/app/libs
cp realm-annotations-processor/build/libs/realm-annotations-processor-${version}.jar distribution/RealmConcurrencyExample/app/libs
mkdir -p distribution/RealmConcurrencyExample/realm
cp -R distribution/realm distribution/RealmConcurrencyExample

echo  "Done"

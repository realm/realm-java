#!/usr/bin/env bash

echo "Building the annotation processor"
./gradlew realm-annotation-processor:assemble

echo "Building Realm"
./gradlew realm:assemble

echo "Copying files to the distribution folder"
cp realm-annotations-processor/build/libs/realm-annotations-processor-0.80.jar distribution
cp realm/build/outputs/aar/realm-0.80.aar distribution/realm

echo "Copying files to the distribution/RealmIntroExample folder"
cp realm-annotations-processor/build/libs/realm-annotations-processor-0.80.jar distribution/RealmIntroExample/app/libs
cp realm/build/outputs/aar/realm-0.80.aar distribution/RealmIntroExample/realm

echo  "Done"

#!/bin/bash

set -e

version=$(cat version.txt)

echo "Cleaning the distribution folder"
git clean -xfd distribution

echo "Building Realm"
./gradlew realm:generateJar realm:javadocReleaseJar

echo "Copying files to the distribution folder"
cp -f changelog.txt distribution
cp realm/build/libs/realm-${version}.jar distribution
cp -R realm/build/docs/javadoc distribution
cp realm/build/libs/realm-${version}-javadoc.jar distribution

echo "Copying files to the distribution/RealmIntroExample folder"
cp -R examples/introExample/src distribution/RealmIntroExample/app
mkdir -p distribution/RealmIntroExample/app/libs
(
  cd distribution/RealmIntroExample/app/libs
  ln -s ../../../realm-${version}.jar .
)

echo "Copying files to the distribution/RealmGridViewExample folder"
cp -R examples/gridViewExample/src distribution/RealmGridViewExample/app
mkdir -p distribution/RealmGridViewExample/app/libs
(
  cd distribution/RealmGridViewExample/app/libs
  ln -s ../../../realm-${version}.jar .
)

echo "Copying files to the distribution/RealmMigrationExample folder"
cp -R examples/migrationExample/src distribution/RealmMigrationExample/app
mkdir -p distribution/RealmMigrationExample/app/libs
(
  cd distribution/RealmMigrationExample/app/libs
  ln -s ../../../realm-${version}.jar .
)

echo "Copying files to the distribution/RealmConcurrencyExample folder"
cp -R examples/concurrencyExample/src distribution/RealmConcurrencyExample/app
mkdir -p distribution/RealmConcurrencyExample/app/libs
(
  cd distribution/RealmConcurrencyExample/app/libs
  ln -s ../../../realm-${version}.jar .
)

echo  "Done"

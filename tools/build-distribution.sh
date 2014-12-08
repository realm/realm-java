#!/bin/bash

set -e

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${script_dir}/.."
version=$(cat version.txt)

echo "Cleaning the distribution folder"
git clean -xfd distribution

echo "Building Realm"
./gradlew realm:androidJar realm:javadocReleaseJar

echo "Copying files to the distribution folder"
cp -f changelog.txt distribution
cp realm/build/libs/realm-${version}.jar distribution
cp -R realm/build/docs/javadoc distribution
cp realm/build/libs/realm-${version}-javadoc.jar distribution

echo "Copying files to the distribution/RealmIntroExample folder"
cp -R examples/introExample/src distribution/RealmIntroExample/app

echo "Copying files to the distribution/RealmGridViewExample folder"
cp -R examples/gridViewExample/src distribution/RealmGridViewExample/app

echo "Copying files to the distribution/RealmMigrationExample folder"
cp -R examples/migrationExample/src distribution/RealmMigrationExample/app

echo "Copying files to the distribution/RealmServiceExample folder"
cp -R examples/serviceExample/src distribution/RealmServiceExample/app

echo "Copying files to the distribution/RealmAdapterExample folder"
cp -R examples/adapterExample/src distribution/RealmAdapterExample/app

echo "Copying files to the distribution/RealmThreadExample folder"
cp -R examples/threadExample/src distribution/RealmThreadExample/app


echo "Creating the Eclipse distribution"
mkdir -p distribution/eclipse
cp realm/build/libs/realm-${version}.jar distribution/eclipse
unzip distribution/eclipse/realm-${version}.jar lib/\* -d distribution/eclipse
zip -d distribution/eclipse/realm-${version}.jar lib/\*
mv distribution/eclipse/lib/* distribution/eclipse/
rm -rf distribution/eclipse/lib

echo "Done"

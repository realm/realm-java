#!/bin/sh

# This script updates Gradle Wrappers in this repository. You need to update the version number in realm.properties first, then execute ./update_gradle_wrapper.sh .

cd "$(dirname $0)/.."

for i in $(find $(pwd) -type f -name gradlew); do
    cd $(dirname $i)
    pwd
    ./gradlew wrapper
    sed -E -i '' s/-bin\\.zip\$/-all.zip/ gradle/wrapper/gradle-wrapper.properties
done

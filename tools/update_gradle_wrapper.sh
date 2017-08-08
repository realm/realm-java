#!/bin/sh

# This script updates Gradle Wrappers in this repository.
# To run it:
#   1. Make sure that you have run ./gradlew assemble in the root directory
#   2. Replace the gradle version number in realm.properties in the root directory, with the new version number.
#   3. Run tools/update_gradle_wrapper.sh

HERE=`pwd`

cd "$(dirname $0)/.."

pushd .

for i in $(find $(pwd) -type f -name gradlew); do
    cd $(dirname $i)
    pwd
    ./gradlew wrapper
    sed -E -i '' s/-bin\\.zip\$/-all.zip/ gradle/wrapper/gradle-wrapper.properties
done

popd

sed -i '' '/^APP_ARGS=/a\
# Realm'"'"'s work-around for a bug in Gradle 4.1 https://github.com/gradle/gradle/issues/2673\
APP_ARGS="\${APP_ARGS} '"'"'--console=plain'"'"' \\\\\
\ "\
' gradlew

cd $HERE


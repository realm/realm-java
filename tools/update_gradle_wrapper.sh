#!/bin/sh

# This script updates Gradle Wrappers in this repository.
# To run it:
#   1. Make sure that you have run ./gradlew assemble in the root directory
#   2. Replace the gradle version number in realm.properties in the root directory, with the new version number.
#   3. Run tools/update_gradle_wrapper.sh

HERE=`pwd`

cd "$(dirname $0)/.."

GRADLE=`grep gradle dependencies.list | cut -d = -f2`
echo "==> Update gradle to version: $GRADLE <=="
echo
read -n1 -r -p "Press any key to continue..." key

for i in $(find $(pwd) -type f -name gradlew); do
    cd $(dirname $i)
    pwd
    ./gradlew wrapper --gradle-version=$GRADLE
done

cd $HERE


#!/bin/sh

# This script will print metrics for the Realm library being deployed to end users.
# To run it:
#   1. Make sure that $D8 is defined in your environment, e.g. `D8="$ANDROID_SDK_ROOT/build-tools/29.0.2/d8"`
#   2. Make sure that you have built the library artifacts using `./gradlew assembleRelease` from the realm folder.
#   3. Run the script: `> sh ./analyze_realm_metrics.sh`
#
# Note: This script has only been tested on MacOS

HERE=`pwd`

cd "$(dirname $0)/.."

cd realm/realm-library/build/outputs/aar

# Base variant
echo "Analyzing Base..."
stat -f"AAR size: %z" realm-android-library-base-release.aar
rm -rf unzippedBase
unzip -qq realm-android-library-base-release.aar -d unzippedBase
sh "$D8" --release --output ./unzippedBase unzippedBase/classes.jar > /dev/null 2>&1
cat ./unzippedBase/classes.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 "Method count: %d\n"'
find ./unzippedBase -name '*.so' -exec stat -f"%z %N" {} \;

# ObjectServer variant
echo "\nAnalyzing ObjectServer..."
stat -f"AAR size: %z" realm-android-library-objectServer-release.aar
rm -rf unzippedObjectServer
unzip -qq realm-android-library-objectServer-release.aar -d unzippedObjectServer
sh "$D8" --release --output ./unzippedObjectServer unzippedObjectServer/classes.jar > /dev/null 2>&1
cat ./unzippedObjectServer/classes.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 "Method count: %d\n"'
find ./unzippedObjectServer -name '*.so' -exec stat -f"%z %N" {} \;

cd $HERE

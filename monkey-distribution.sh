#!/bin/bash
#
# Runs Android monkey tool on all distribution projects. Using this script before using
# build-distribution.sh will fail.
#
# Note: adb shell always return exit code 0: https://code.google.com/p/android/issues/detail?id=3254
# Solution for now: Write exit code to file and read it from there
#

TEST_EVENTS=2000;

echo ""
echo "Testing distribution examples"
echo ""
for dist in distribution/Realm*/ ; do

    applicationId=$(grep applicationId "${dist}app/build.gradle" | cut -d \" -f 2 | cut -d \' -f 2)
    project=$(basename ${dist})

    echo "Building ${dist}"
    adb uninstall ${applicationId} > /dev/null
    cd ${dist}
    ./gradlew clean installDebug
    cd -

    echo "Letting monkey loose in $dist"
    adb shell "monkey -p ${applicationId} -v ${TEST_EVENTS} ; echo \"\$?\\c\" > /data/local/tmp/${applicationId}.exitcode"
    rc=$(adb shell cat /data/local/tmp/${applicationId}.exitcode)
    echo ""
    if [ "${rc}" != "0" ] ; then
        echo ""
        echo "Monkey found an error, stopping tests."
        echo "Remember to call build-distribution.sh before running this test."
        echo "Reproduce: adb shell monkey -s <seed> -p ${applicationId} -v ${TEST_EVENTS}"
        echo "Exit code: ${rc}"
        exit 1
    fi
done

echo ""
echo "Distribution monkeys are happy"
echo ""
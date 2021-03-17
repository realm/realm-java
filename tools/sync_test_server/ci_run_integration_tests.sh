#!/bin/bash
# Script for running integration tests on CI
SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

echo "Start building local BAAS"
cd $SCRIPTPATH/baas_local
bash ./start_local_server.sh
cd ../../../realm
adb logcat -b all -c
adb logcat -v time > $SCRIPTPATH/logcat.txt &
LOG_CAT_PID=`echo \$!`
# TODO: Build parameters should be command line parameters
./gradlew connectedObjectServerDebugAndroidTest -PbuildTargetABIs=x86 -PenableLTO=false -PbuildCore=true
EXIT_CODE=`echo $?`
echo "Android tests exit code: $EXIT_CODE"
kill $LOG_CAT_PID
cd $SCRIPTPATH/baas_local
bash ./stop_local_server.sh
echo "Local servers stopped"
exit $EXIT_CODE
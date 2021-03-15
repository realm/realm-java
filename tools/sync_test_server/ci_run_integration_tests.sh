#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

echo "Start building local BAAS"
cd $SCRIPTPATH/baas_local
bash ./start_local_server.sh
cd ../../../realm
# TODO: Support different flags and tests for full release buils
./gradlew connectedObjectServerDebugAndroidTest -PbuildTargetABIs=x86 -PenableLTO=false -PbuildCore=true
EXIT_CODE=`echo $?`
print "Android tests exit code: $EXIT_CODE"
cd $SCRIPTPATH/baas_local
bash ./stop_local_server.sh
echo "Local servers stopped"
exit $EXIT_CODE
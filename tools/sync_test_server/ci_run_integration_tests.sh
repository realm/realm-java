#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

cd $SCRIPTPATH/baas_local
./start_local_server.sh
cd ../../../realm
./gradlew connectedBaseDebugAndroidTest -PbuildTargetABIs=x86 -PenableLTO=false -PbuildCore=true

#!/bin/sh

# Get the script dir which contains the Dockerfile
DOCKERFILE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

TMP_DIR=$(mktemp -d /tmp/sync-test.XXXX) || { echo "Failed to mktemp $TEST_TEMP_DIR" ; exit 1 ; }

adb reverse tcp:7800 tcp:7800 && \
adb reverse tcp:8080 tcp:8080 && \
adb reverse tcp:8888 tcp:8888 || { echo "Failed to reverse adb port." ; exit 1 ; }

docker build $DOCKERFILE_DIR -t sync-test-server || { echo "Failed to build Docker image." ; exit 1 ; }

echo "See log files in $TMP_DIR"
docker run -p 8080:8080 -p 7800:7800 -p 8888:8888 -v$TMP_DIR:/tmp --name sync-test-server --entrypoint /usr/bin/ros-testing-server.js sync-test-server /tmp/ros-testing-server.log

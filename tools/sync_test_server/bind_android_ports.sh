#!/usr/bin/env bash

# This scripts tries to bind the android emulator ports to point the local development server.

adb reverse tcp:9443 tcp:9443 && \
adb reverse tcp:9080 tcp:9080 && \
adb reverse tcp:9090 tcp:9090 && \
adb reverse tcp:8888 tcp:8888 && \
echo "Done" || { echo "Failed to reverse android emulator ports." ;}

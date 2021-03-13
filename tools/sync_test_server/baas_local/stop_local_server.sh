#!/bin/bash

#
# Stops any running BAAS and command server
#
SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

if [[ -f $SCRIPTPATH/tmp-command-server/command_server.pid ]]; then
    PIDS_TO_KILL="$(< $SCRIPTPATH/tmp-command-server/command_server.pid)"
fi
if [[ -f $SCRIPTPATH/tmp-baas/baas_server.pid ]]; then
    PIDS_TO_KILL="$(< $SCRIPTPATH/tmp-baas/baas_server.pid) $PIDS_TO_KILL"
fi
if [[ -f $SCRIPTPATH/tmp-baas/baas_ready ]]; then
    rm $SCRIPTPATH/tmp-baas/baas_ready
fi
if [[ -n "$PIDS_TO_KILL" ]]; then
    echo "Killing $PIDS_TO_KILL"
    kill -9 $PIDS_TO_KILL
fi
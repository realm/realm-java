#!/bin/sh

WORK_PATH="$HOME/.realm_baas_java"
BAAS_PID=""
MONGOD_PID=""
if [[ -f $WORK_PATH/baas_server.pid ]]; then
    BAAS_PID="$(< "$WORK_PATH/baas_server.pid")"
fi

if [[ -f $WORK_PATH/mongod.pid ]]; then
    MONGOD_PID="$(< "$WORK_PATH/mongod.pid")"
fi

if [[ -n "$BAAS_PID" ]]; then
    echo "Stopping baas $BAAS_PID"
    kill -9 "$BAAS_PID"
    rm $WORK_PATH/baas_server.pid
fi


if [[ -n "$MONGOD_PID" ]]; then
    echo "Killing mongod $MONGOD_PID"
    kill -9 "$MONGOD_PID"
    rm $WORK_PATH/mongod.pid
fi

docker stop mongodb-realm-command-server -t0

#!/bin/bash

# This script provides a single entry point to the different tools used during the
# realm-java development. 
#
# It needs REALM_JAVA_PATH to be set to the realm-java repository path.
# 
# Create an alias for convenient access, for example in .zshrc:
# alias realm="~/realm-java/tools/realm-cli.sh"


is_server_active(){
    return `docker container ls | grep -Fq mongodb-realm`
}

start_server(){
    if is_server_active
    then
        echo "Warning: Sync server already runnning"
    else
        echo -n "Starting sync server..."
        $REALM_JAVA_PATH/tools/sync_test_server/start_server.sh > /dev/null 2>&1
        echo " done"
    fi
}

stop_server(){
    if is_server_active
    then
        echo -n "Stopping sync server..."
        $REALM_JAVA_PATH/tools/sync_test_server/stop_server.sh > /dev/null 2>&1
        echo " done"
    else
        echo "Warning: Sync server not running"
    fi
}

restart_server(){
    if is_server_active
    then
        stop_server
        start_server
    else
        echo "Warning: Sync server wasn't running"
        start_server
    fi
}

server_status(){
    if is_server_active
    then
        echo "Sync server: ON"
    else
        echo "Sync server: OFF"
    fi
}

bind_server(){
    echo -n "Forwarding ports... "
    adb reverse tcp:9443 tcp:9443 && \
    adb reverse tcp:9080 tcp:9080 && \
    adb reverse tcp:9090 tcp:9090 && \
    adb reverse tcp:8888 tcp:8888 && \
    echo "done" || { echo "failed" ; exit 1 ; }
}

server_help(){
    echo "Try with:

start   - starts the sync server
stop    - stops the sync server
restart - restarts the sync server
status  - shows the sync server status
bind    - bind the emulator ports to the sync server"
}

server(){
    action=$2
    case $action in
        start)
            start_server
            ;;
        stop)
            stop_server
            ;;
        restart)
            restart_server
            ;;
        status)
            server_status
            ;;
        bind)
            bind_server
            ;;
        *)
            server_help
    esac
}

java_install(){
    pushd $REALM_JAVA_PATH
    ./gradlew installRealmJava
    popd
}

java_build(){
    pushd $REALM_JAVA_PATH
    ./gradlew assemble --stacktrace
    popd
}

java_test(){
    pushd $REALM_JAVA_PATH/realm
    ./gradlew connectedObjectServerDebugAndroidTest --stacktrace
    popd
}

java_check(){
    pushd $REALM_JAVA_PATH/realm
    ./gradlew spotbugsMain pmd checkstyle
    popd
}

java_clean(){
    pushd $REALM_JAVA_PATH
    ./gradlew clean
    popd
}

java_help(){
    echo "Try with:

install - install realm-java locally
build   - builds realm-java
test    - runs the realm-java test suite
check   - runs realm-java spotbugs, checkstyle, pmd
clean   - cleans realm-java"
}

java(){
    action=$2
    case $action in
        install)
            java_install
            ;;
        build)
            java_build
            ;;
        test)
            java_test
            ;;
        check)
            java_check
            ;;
        clean)
            java_clean
            ;;
        *)
            java_help
    esac
}

show_help(){
    echo "Try with:

server - controls the sync server
java    - executes realm-java actions"
}

if ! [[ -n "${REALM_JAVA_PATH}" ]]; then
    echo "Error: \$REALM_JAVA_PATH not defined."
    exit 1
fi

command=$1
case $command in
    server)
    server $*
    ;;
    java)
    java $*
    ;;
    *)
    show_help
esac

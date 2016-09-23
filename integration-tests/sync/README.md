# RUNNING THE TESTSERVER

This document describes how to configure and start the test server used by the integration tests.
This description is only temporary. We should find a better solution.

## HOW TO

1. Test server server needs to be started before running the integration test.

a) Install docker.
b) Build the docker image:
   The `token` is to access to our private yum repo. Ask mc for it.

```sh
# From the project root dir
docker build integration-tests/sync/test_server --build-arg ACCESS_TOKEN=<token>-t sync-test
```

2. Start the test server

a) Start the docker:

```sh
docker run  -p 8080:8080 -p 7800:7800 -p 8888:8888 sync-test /bin/sh -c "ros-testing-server /tmp/some.log"
```

b) stop the docker:

```
# Get the running docker id
docker stats
#docker stop -t1 <container-id>
```

# Future plans

The goal is to have standalone integration tests.

This means that the test suite should be able to download and run the required server automatically. Also the above
link only points to server binaries for Mac OSX. The tests should run on any platform.

An initial guess is that we should switch to using the node.js server instead but that still needs to be investigated.
If not we should create a gradle task that automatically downloads, unpacks and runs the Mac OS X server just like
we do for the core file.

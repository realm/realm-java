# RUNNING THE TESTSERVER

This document describes how to configure and start the test server used by the integration tests.
This description is only temporary. We should find a better solution.

## HOW TO

1. Test server server needs to be started before running the integration test.

a) Download the matching server version from S3: `s3://ealm-ci-artifacts/sync/<version>/cocoa/realm-sync-server-<version>.zip`
b) Extract the files to `./realm-sync-server`


2. Start the test server

a) Run `sh start.sh`


# Future plans

The goal is to have standalone integration tests.

This means that the test suite should be able to download and run the required server automatically. Also the above
link only points to server binaries for Mac OSX. The tests should run on any platform.

An initial guess is that we should switch to using the node.js server instead but that still needs to be investigated.
If not we should create a gradle task that automatically downloads, unpacks and runs the Mac OS X server just like
we do for the core file.

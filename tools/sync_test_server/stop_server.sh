#!/bin/sh

docker stop mongodb-realm -t0
docker stop mongodb-realm-cli -t0
docker network rm mongodb-realm-network
# docker rm mongodb-realm // What does this do?

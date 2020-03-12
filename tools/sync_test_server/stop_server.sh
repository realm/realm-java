#!/bin/sh

docker stop mongodb-realm -t0
docker stop mongodb-realm-cli -t0
docker stop mongodb-realm-command-server -t0
docker rm mongodb-realm-command-server
docker network rm mongodb-realm-network

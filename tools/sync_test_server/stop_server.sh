#!/bin/sh

docker stop mongodb-realm -t0
docker stop mongodb-realm-command-server -t0
docker network rm mongodb-realm-network

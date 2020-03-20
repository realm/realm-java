#!/bin/sh

# Verify that Github username and tokens are available as environment vars
if [[ -z "${GITHUB_USER}" ]]; then
  echo "Could not find \$GITHUB_USER as an environment variabel"
  exit 1
fi

if [[ -z "${GITHUB_DOCKER_TOKEN}" ]]; then
  echo "Could not find \$GITHUB_DOCKER_TOKEN as an environment variabel. This is used to download Docker Registry packages."
  exit 1
fi

# Get the script dir which contains the Dockerfile
DOCKERFILE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

MONGODB_REALM_VERSION=$(grep MONGODB_REALM_SERVER_VERSION $DOCKERFILE_DIR/../../dependencies.list | cut -d'=' -f2)

adb reverse tcp:9443 tcp:9443 && \
adb reverse tcp:9080 tcp:9080 && \
adb reverse tcp:9090 tcp:9090 && \
adb reverse tcp:8888 tcp:8888 || { echo "Failed to reverse adb port." ; exit 1 ; }

# Make sure that Docker works correctly with Github Docker Registry by logging in
docker login docker.pkg.github.com -u $GITHUB_USER -p $GITHUB_DOCKER_TOKEN

# Run Stitch and Stitch CLI Docker images
docker network create mongodb-realm-network
docker build $DOCKERFILE_DIR -t mongodb-realm-command-server || { echo "Failed to build Docker image." ; exit 1 ; }
ID=$(docker run --rm -i -t -d --network mongodb-realm-network -p 9090:9090 -p 8888:8888 --name mongodb-realm docker.pkg.github.com/realm/ci/mongodb-realm-test-server:latest)
docker run --rm -i -t -d --network container:$ID -v$TMP_DIR:/tmp --name mongodb-realm-command-server mongodb-realm-command-server

docker cp "$DOCKERFILE_DIR"/app_config mongodb-realm:/tmp/app_config
docker cp "$DOCKERFILE_DIR"/setup_mongodb_realm.sh mongodb-realm:/tmp/
docker exec -it mongodb-realm sh /tmp/setup_mongodb_realm.sh


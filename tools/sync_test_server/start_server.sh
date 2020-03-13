#!/bin/sh

# Get the script dir which contains the Dockerfile
DOCKERFILE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

MONGODB_REALM_VERSION=$(grep MONGODB_REALM_SERVER_VERSION $DOCKERFILE_DIR/../../dependencies.list | cut -d'=' -f2)

adb reverse tcp:9443 tcp:9443 && \
adb reverse tcp:9080 tcp:9080 && \
adb reverse tcp:9090 tcp:9090 && \
adb reverse tcp:8888 tcp:8888 || { echo "Failed to reverse adb port." ; exit 1 ; }

# Make sure that Docker works correctly with AWS by logging in
DOCKER_LOGIN=$(aws ecr get-login --no-include-email)
eval $DOCKER_LOGIN

# Work-around for getting latest Stich image
LATEST_MONGODB_REALM_VERSION=$(aws ecr describe-images --repository-name ci/mongodb-realm-images --query 'sort_by(imageDetails,& imagePushedAt)[-1].imageTags[0]' | cut -d '"' -f 2)
LATEST_CLI_VERSION="190"

# Run Stitch and Stitch CLI Docker images
docker network create mongodb-realm-network
docker build $DOCKERFILE_DIR -t mongodb-realm-command-server || { echo "Failed to build Docker image." ; exit 1 ; }
ID=$(docker run --rm -i -t -d --network mongodb-realm-network  -p 8888:8888 -p 9090:9090 --name mongodb-realm 012067661104.dkr.ecr.eu-west-1.amazonaws.com/ci/mongodb-realm-images:"$LATEST_MONGODB_REALM_VERSION")
docker run --rm -i -t -d --network container:$ID -v$TMP_DIR:/tmp --name mongodb-realm-command-server mongodb-realm-command-server
docker run --rm -i -t -d --network container:$ID --name mongodb-realm-cli 012067661104.dkr.ecr.eu-west-1.amazonaws.com/ci/stitch-cli:"$LATEST_CLI_VERSION"

docker cp "$DOCKERFILE_DIR"/app_config mongodb-realm-cli:/tmp/app_config
docker cp "$DOCKERFILE_DIR"/setup_mongodb_realm.sh mongodb-realm-cli:/tmp/
docker exec -it mongodb-realm-cli sh /tmp/setup_mongodb_realm.sh

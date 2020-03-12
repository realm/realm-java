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
docker run --rm -i -t --publish 9090:9090 -d --name mongodb-realm --network mongodb-realm-network 012067661104.dkr.ecr.eu-west-1.amazonaws.com/ci/mongodb-realm-images:"$LATEST_MONGODB_REALM_VERSION"
docker run --rm -i -t -d --name mongodb-realm-cli --network mongodb-realm-network 012067661104.dkr.ecr.eu-west-1.amazonaws.com/ci/stitch-cli:"$LATEST_CLI_VERSION"
docker build $DOCKERFILE_DIR -t mongodb-realm-command-server || { echo "Failed to build Docker image." ; exit 1 ; }
docker run -p 8888:8888 -v$TMP_DIR:/tmp --name mongodb-realm-command-server mongodb-realm-command-server

docker cp "$DOCKERFILE_DIR"/app_config mongodb-realm-cli:/project/app_config
docker cp "$DOCKERFILE_DIR"/setup_mongodb_realm.sh mongodb-realm-cli:/project/
docker exec -it mongodb-realm-cli sh /project/setup_mongodb_realm.sh

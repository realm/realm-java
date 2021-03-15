#!/bin/sh

# How to use this script:
#
# 1. Logging into GitHub
# 2. Goto "Settings > Developer Settings > Personal access tokens"
# 3. Press "Generate new Token"
# 4. Select "read:packages" as Scope. Give it a name and create the token.
# 5. Store the token in a environment variable called GITHUB_DOCKER_TOKEN.
# 6. Store the GitHub username in an environment variable called GITHUB_DOCKER_USER.
# 7. Run this script.

# Verify that Github username and tokens are available as environment vars
if [[ -z "${GITHUB_DOCKER_USER}" ]]; then
  echo "Could not find \$GITHUB_DOCKER_USER as an environment variable"
  exit 1
fi

if [[ -z "${GITHUB_DOCKER_TOKEN}" ]]; then
  echo "Could not find \$GITHUB_DOCKER_TOKEN as an environment variable. This is used to download Docker Registry packages."
  exit 1
fi

# Get the script dir which contains the Dockerfile
DOCKERFILE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

MONGODB_REALM_VERSION=$(grep MONGODB_REALM_SERVER $DOCKERFILE_DIR/../../dependencies.list | cut -d'=' -f2)

adb reverse tcp:9443 tcp:9443 && \
adb reverse tcp:9080 tcp:9080 && \
adb reverse tcp:9090 tcp:9090 && \
adb reverse tcp:8888 tcp:8888 || { echo "Failed to reverse adb port." ; exit 1 ; }

# Make sure that Docker works correctly with Github Docker Registry by logging in
docker login docker.pkg.github.com -u $GITHUB_DOCKER_USER -p $GITHUB_DOCKER_TOKEN

SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

# Create app configurations
APP_CONFIG_DIR=`mktemp -d -t app_config`
$SCRIPTPATH/app_config_generator.sh $APP_CONFIG_DIR $SCRIPTPATH/app_template testapp1 testapp2

# Run Stitch and Stitch CLI Docker images
docker network create mongodb-realm-network
docker build $DOCKERFILE_DIR -t mongodb-realm-command-server || { echo "Failed to build Docker image." ; exit 1 ; }
ID=$(docker run --rm -i -t -d -v$APP_CONFIG_DIR:/apps --network mongodb-realm-network -p9090:9090 -p8888:8888 -p26000:26000 --name mongodb-realm docker.pkg.github.com/realm/ci/mongodb-realm-test-server:$MONGODB_REALM_VERSION)
docker run --rm -i -t -d --network container:$ID -v$APP_CONFIG_DIR:/apps -v$TMP_DIR:/tmp --name mongodb-realm-command-server mongodb-realm-command-server

echo "Template apps are generated in/served from $APP_CONFIG_DIR"

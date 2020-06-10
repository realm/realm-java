#!/bin/sh

#
# This script is inteded to run within the mongodb-realm-cli Docker image and will setup and
# configure Stitch so it is ready for running integration tests against.
#
# If you need to re-configure Stitch, do the following
#
# 1) Run this script to start a Stitch instance
# 2) Start a browser and go to: http://127.0.0.1:9090
# 3) Log in with "unique_user@domain.com" and "password"
# 4) Select the only Group ID there is and choose the App starting with "realm-sdk-integration-tests"
# 5) Make modifications as required
# 6) Export the app again in "Manage > Deploy > Import/Export App"
# 7) Unpack the zip file and replace the "app_config" folder.
# 8) Rerun this script
#

cd /tmp

echo "Waiting for Stitch to start"
while ! curl --output /dev/null --silent --head --fail http://localhost:9090; do
  sleep 1 && echo -n .;
done;

ACCESS_TOKEN=$(curl --request POST --header "Content-Type: application/json" --data '{ "username":"unique_user@domain.com", "password":"password" }' http://localhost:9090/api/admin/v3.0/auth/providers/local-userpass/login -s | jq ".access_token" -r)
GROUP_ID=$(curl --header "Authorization: Bearer $ACCESS_TOKEN" http://localhost:9090/api/admin/v3.0/auth/profile -s | jq '.roles[0].group_id' -r)

# Enable for debug
# echo "Access token: $ACCESS_TOKEN"
echo "Group Id: $GROUP_ID"

# 1. Log in to enable Stitch CLI commands
yes | stitch-cli login --config-path=/tmp/stitch-config \
                 --base-url=http://localhost:9090 \
                 --auth-provider=local-userpass \
                 --username=unique_user@domain.com \
                 --password=password

# 2. Attempt to import project. It will fail because of lacking secret, but create the App ID
#    which we need to extract from the commandline output
IMPORT_RESPONSE=$(stitch-cli import \
                  --config-path=/tmp/stitch-config \
                  --base-url=http://localhost:9090 \
                  --path=/tmp/app_config \
                  --app-name realm-sdk-integration-tests \
                  --project-id "$GROUP_ID" \
                  --strategy replace \
                  -y)
APP_ID_SUFFIX=$(echo "$IMPORT_RESPONSE" | grep "New app created:" | cut -d ':' -f 2 | cut -d '-' -f 5)
echo "App ID Suffix: $APP_ID_SUFFIX"

# 3. Create the secret(s) needed to start the Stitch app:
#    - a) MongoDB Service: Requires an URI.
stitch-cli secrets add \
                  --name="BackingDB_uri" \
                  --value="mongodb://localhost:26000" \
                  --app-id="realm-sdk-integration-tests-$APP_ID_SUFFIX" \
                  --base-url=http://localhost:9090 \
                  --config-path=/tmp/stitch-config

#    - b) Firebase Cloud Messaging: create your own app and paste the "server key" here
stitch-cli secrets add \
                  --name="FCM_SERVER_KEY" \
                  --value="AAAAQLogIEA:APA91bGq5nvqcwR0NE3aQKADal14gP9sFS9MJ28JFqXeJ8h8kffHUaQ2GnpWFzbPRRCQn4P2Ux_HGTWJ9xxgZUmkv-Stcl4-Nbj3vcLOQP253wdzsQsc3dSIDKG53eWxTXWMr0_uleAE" \
                  --app-id="realm-sdk-integration-tests-$APP_ID_SUFFIX" \
                  --base-url=http://localhost:9090 \
                  --config-path=/tmp/stitch-config

#    - c) Firebase Cloud Messaging: create your own app and paste the "sender ID" here
stitch-cli secrets add \
                  --name="FCM_SENDER_ID" \
                  --value="278000574528" \
                  --app-id="realm-sdk-integration-tests-$APP_ID_SUFFIX" \
                  --base-url=http://localhost:9090 \
                  --config-path=/tmp/stitch-config

# 4. Now we can correctly import the Stitch app
stitch-cli import \
                  --config-path=/tmp/stitch-config \
                  --base-url=http://localhost:9090 \
                  --path=/tmp/app_config \
                  --app-name realm-sdk-integration-tests \
                  --project-id "$GROUP_ID" \
                  --strategy replace \
                  -y

# 5. Store the application id in the Command Server so it can be accessed by Integration Tests on the device
curl -X PUT -d id="realm-sdk-integration-tests-$APP_ID_SUFFIX" http://localhost:8888/application-id

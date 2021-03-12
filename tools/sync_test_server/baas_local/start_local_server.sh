#!/bin/bash
#
# This script controls starting a MongoDB Realm test server and import two running apps into it.
#
# It also starts a local command server that the integration tests use to control the MongoDB Realm
# instance.
#
#
# Copy from https://github.com/realm/ci/edit/master/realm/docker/mongodb-realm/run.sh
# Requires jq and mongodb-realm-cli

set -e

function import_apps () {
  app_dir="$1"
  realm-cli login --config-path=/tmp/stitch-config --base-url=http://localhost:9090 --auth-provider=local-userpass --username=unique_user@domain.com --password=password
  access_token=$(yq e ".access_token" /tmp/stitch-config)
  group_id=$(curl --header "Authorization: Bearer $access_token" http://localhost:9090/api/admin/v3.0/auth/profile -s | jq '.roles[0].group_id' -r)
  cd $app_dir
  for app in *; do
      echo "importing app: ${app}"

      manifest_file="config.json"
      app_id_param=""
      if [ -f "$app/secrets.json" ]; then
          # create app by importing an empty skeleton with the same name
          app_name=$(jq '.name' "$app/$manifest_file" -r)
          temp_app="/tmp/stitch-apps/$app"
          mkdir -p "$temp_app" && echo "{ \"name\": \"$app_name\" }" > "$temp_app/$manifest_file"
          realm-cli import --config-path=/tmp/stitch-config --base-url=http://localhost:9090 --path="$temp_app" --project-id $group_id -y --strategy replace

          app_id=$(jq '.app_id' "$temp_app/$manifest_file" -r)
          app_id_param="--app-id=$app_id"

          # import secrets into the created app
          while read -r secret value; do
              realm-cli secrets add --config-path=/tmp/stitch-config --base-url=http://localhost:9090 --app-id=$app_id --name="$secret" --value="$(echo $value | sed 's/\\n/\n/g')"
          done < <(jq 'to_entries[] | [.key, .value] | @tsv' "$app/secrets.json" -r)
      fi

      realm-cli import --config-path=/tmp/stitch-config --base-url=http://localhost:9090 --path="$app" $app_id_param --project-id $group_id -y --strategy replace
      jq '.app_id' "$app/$manifest_file" -r > "$app/app_id"
  done
}

SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
MONGODB_REALM_VERSION=$(grep MONGODB_REALM_SERVER $SCRIPTPATH/../../../dependencies.list | cut -d'=' -f2)

adb reverse tcp:9443 tcp:9443 && \
adb reverse tcp:9080 tcp:9080 && \
adb reverse tcp:9090 tcp:9090 && \
adb reverse tcp:8888 tcp:8888 || { echo "Failed to reverse adb port." ; exit 1 ; }

SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

# Cleanup any old installation
echo "Cleanup old tmp directories..."
if [[ -d tmp-command-server ]]; then
    rm -rf tmp-command-server
fi
if [[ -d tmp-baas ]]; then
    rm -rf tmp-baas
fi

# Install and run BAAS
echo "Install and start BAAS..."
sh ./install_baas.sh ./tmp-baas & #$MONGODB_REALM_VERSION
while [[ ! -e $SCRIPTPATH/tmp-baas/baas_ready ]] ; do
    sleep 1
done

# Create app configurations
echo "Prepare app templates..."
APP_CONFIG_DIR=`mktemp -d -t app_config`
$SCRIPTPATH/../app_config_generator.sh $APP_CONFIG_DIR $SCRIPTPATH/../app_template testapp1 testapp2

echo "Import apps..."
import_apps "$APP_CONFIG_DIR"
cd $SCRIPTPATH

# Start command server
echo "Start command server..."
mkdir tmp-command-server || true
cd tmp-command-server
cp $SCRIPTPATH/../mongodb-realm-command-server.js ./
npm install winston@2.4.0 temp httpdispatcher@1.0.0 fs-extra moment is-port-available@0.1.5
node ./mongodb-realm-command-server.js $APP_CONFIG_DIR &
echo $! > command_server.pid

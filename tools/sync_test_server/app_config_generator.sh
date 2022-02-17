#!/bin/bash
TARGET_APP_PATH=$1;shift
TEMPLATE_APP_PATH=$1;shift
SYNC_MODE=$1;shift # Must be either "partition" or "flex"
mkdir -p $TARGET_APP_PATH
for APP_NAME in "$@"
do
    cp -r $TEMPLATE_APP_PATH $TARGET_APP_PATH/$APP_NAME
    sed -i'.bak' 's/APP_NAME_PLACEHOLDER/'$APP_NAME'/g' $TARGET_APP_PATH/$APP_NAME/config.json
done

# Setup sync configuration
for APP_NAME in "$@"
do
    JSON="boo"
    if [ "$SYNC_MODE" = "partition" ]; then
      JSON='
        "sync": {
            "state": "enabled",
            "database_name": "test_data",
            "partition": {
                "key": "realm_id",
                "type": "string",
                "permissions": {
                    "read": true,
                    "write": true
                }
            }
        }
      '
    fi
    if [ "$SYNC_MODE" = "flex" ]; then
      JSON='
        "flexible_sync": {
            "state": "enabled",
            "database_name": "test_data",
            "queryable_fields_names": [
                "age",
                "name",
                "color",
                "section"
            ]
        }
      '
    fi

    ESCAPED_JSON=`echo ${JSON} | tr '\n' "\\n"`
    cp -r $TEMPLATE_APP_PATH $TARGET_APP_PATH/$APP_NAME
    sed -i'.bak' "s/%SYNC_CONFIG%/$ESCAPED_JSON/g" $TARGET_APP_PATH/$APP_NAME/services/BackingDB/config.json
done

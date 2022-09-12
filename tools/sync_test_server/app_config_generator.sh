#!/bin/bash
TARGET_APP_PATH=$1;shift
TEMPLATE_APP_PATH=$1;shift
SYNC_MODE=$1;shift # Must be either "partition" or "flex"
AUTH_MODE=$1;shift # Must be either "auto", "function" or "email"
mkdir -p $TARGET_APP_PATH
for APP_NAME in "$@"
do
    cp -r $TEMPLATE_APP_PATH $TARGET_APP_PATH/$APP_NAME
    sed -i'.bak' 's/APP_NAME_PLACEHOLDER/'$APP_NAME'/g' $TARGET_APP_PATH/$APP_NAME/config.json
done

# Setup auth config
for APP_NAME in "$@" 
do
    JSON="placeholder"
    if [ "$AUTH_MODE" = "auto" ]; then
        JSON='
            "config": {
                "autoConfirm": true,
                "runConfirmationFunction": false,
                "confirmationFunctionName": "confirmFunc",
                "emailConfirmationUrl": "http://realm.io/confirm-user",
                "resetFunctionName": "resetFunc",
                "resetPasswordSubject": "Reset Password",
                "resetPasswordUrl": "http://realm.io/reset-password",
                "runResetFunction": false
            },
        '
    fi
    if [ "$AUTH_MODE" = "function" ]; then
        JSON='
            "config": {
                "autoConfirm": false,
                "runConfirmationFunction": true,
                "confirmationFunctionName": "confirmFunc",
                "emailConfirmationUrl": "http://realm.io/confirm-user",
                "resetFunctionName": "resetFunc",
                "resetPasswordSubject": "Reset Password",
                "resetPasswordUrl": "http://realm.io/reset-password",
                "runResetFunction": false
            },
        '
    fi
    if [ "$AUTH_MODE" = "email" ]; then
        JSON='
            "config": {
                "autoConfirm": false,
                "runConfirmationFunction": false,
                "confirmationFunctionName": "confirmFunc",
                "emailConfirmationUrl": "http://realm.io/confirm-user",
                "resetFunctionName": "resetFunc",
                "resetPasswordSubject": "Reset Password",
                "resetPasswordUrl": "http://realm.io/reset-password",
                "runResetFunction": false
            },
        '
    fi

    ESCAPED_JSON=`echo ${JSON} | tr '\n' "\\n"`     
    cp -r $TEMPLATE_APP_PATH $TARGET_APP_PATH/$APP_NAME
    sed -i'.bak' "s#%EMAIL_AUTH_CONFIG%#$ESCAPED_JSON#g" $TARGET_APP_PATH/$APP_NAME/auth_providers/local-userpass.json
done

# Setup sync configuration
for APP_NAME in "$@"
do
    JSON="placeholder"
    if [ "$SYNC_MODE" = "partition" ]; then
      JSON='
        "sync": {
            "state": "enabled",
            "database_name": "test_data",
            "partition": {
                "key": "realm_id",
                "type": "string",
                "permissions": {
                    "read": {
                        "%%true": {
                            "%function": {
                                "arguments": [
                                    "%%partition"
                                ],
                                "name": "canReadPartition"
                            }
                        }
                    },
                    "write": {
                        "%%true": {
                            "%function": {
                                "arguments": [
                                    "%%partition"
                                ],
                                "name": "canWritePartition"
                            }
                        }
                    }
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
            ],
            "permissions": {
                "rules": {},
                "defaultRoles": [
                    {
                        "name": "read-write",
                        "applyWhen": {},
                        "read": true,
                        "write": true
                    }
                ]
            }
        }
      '
    fi

    ESCAPED_JSON=`echo ${JSON} | tr '\n' "\\n"`
    cp -r $TEMPLATE_APP_PATH $TARGET_APP_PATH/$APP_NAME
    sed -i'.bak' "s/%SYNC_CONFIG%/$ESCAPED_JSON/g" $TARGET_APP_PATH/$APP_NAME/services/BackingDB/config.json
done

#!/bin/bash -xe
TARGET_APP_PATH=$1;shift
TEMPLATE_APP_PATH=$1;shift
mkdir -p $TARGET_APP_PATH
for APP_ID in "$@"
do
    APP_PATH=`echo $APP_ID | cut -f1 -d-`
    cp -r $TEMPLATE_APP_PATH $TARGET_APP_PATH/$APP_PATH
    sed -i '.bak' 's/APP_ID_PLACEHOLDER/'$APP_ID'/g' $TARGET_APP_PATH/$APP_PATH/config.json
done

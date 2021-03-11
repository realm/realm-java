#!/bin/bash
# Clone of https://raw.githubusercontent.com/realm/realm-core/master/evergreen/install_baas.sh
# But removed the part that import apps.
#
# This script will download all the dependencies for and build/start a Realm Cloud app server.
#
# Usage:
# ./evergreen/build_and_run_baas.sh {path to working directory} [git revision of baas]
#

set -o errexit
set -o pipefail

# Go and CCache on Mac does not play well together
unset -v CC

case $(uname -s) in
    Darwin)
        STITCH_SUPPORT_LIB_URL="https://s3.amazonaws.com/stitch-artifacts/stitch-support/stitch-support-macos-debug-4.3.2-721-ge791a2e-patch-5e2a6ad2a4cf473ae2e67b09.tgz"
        STITCH_ASSISTED_AGG_URL="https://stitch-artifacts.s3.amazonaws.com/stitch-mongo-libs/stitch_mongo_libs_osx_ac073d06065af6e00103a8a3cf64672a3f875bea_20_12_01_19_47_16/assisted_agg"
        GO_URL="https://golang.org/dl/go1.14.10.darwin-amd64.tar.gz"
        MONGODB_DOWNLOAD_URL="http://downloads.10gen.com/osx/mongodb-macos-x86_64-enterprise-4.4.1.tgz"
        YQ_DOWNLOAD_URL="https://github.com/mikefarah/yq/releases/download/3.4.1/yq_darwin_amd64"
        JQ_DOWNLOAD_URL="https://github.com/stedolan/jq/releases/download/jq-1.6/jq-osx-amd64"
    ;;
    Linux)
        GO_URL="https://golang.org/dl/go1.14.10.linux-amd64.tar.gz"
        YQ_DOWNLOAD_URL="https://github.com/mikefarah/yq/releases/download/3.4.1/yq_linux_amd64"
        JQ_DOWNLOAD_URL="https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64"

        # Detect what distro/versionf of Linux we are running on to download the right version of MongoDB to download
        # /etc/os-release covers debian/ubuntu/suse
        if [[ -e /etc/os-release ]]; then
            # Amazon Linux 2 comes back as 'amzn'
            DISTRO_NAME="$(. /etc/os-release ; echo "$ID")"
            DISTRO_VERSION="$(. /etc/os-release ; echo "$VERSION_ID")"
            DISTRO_VERSION_MAJOR="$(cut -d. -f1 <<< "$DISTRO_VERSION" )"
        elif [[ -e /etc/redhat-release ]]; then
            # /etc/redhat-release covers RHEL
            DISTRO_NAME=rhel
            DISTRO_VERSION="$(lsb_release -s -r)"
            DISTRO_VERSION_MAJOR="$(cut -d. -f1 <<< "$DISTRO_VERSION" )"
        fi
        case $DISTRO_NAME in
            ubuntu)
                MONGODB_DOWNLOAD_URL="http://downloads.10gen.com/linux/mongodb-linux-$(uname -m)-enterprise-ubuntu${DISTRO_VERSION_MAJOR}04-4.4.1.tgz"
		STITCH_ASSISTED_AGG_LIB_URL="https://stitch-artifacts.s3.amazonaws.com/stitch-mongo-libs/stitch_mongo_libs_ubuntu2004_x86_64_ac073d06065af6e00103a8a3cf64672a3f875bea_20_12_01_19_47_16/libmongo-ubuntu2004-x86_64.so"
                STITCH_SUPPORT_LIB_URL="https://mciuploads.s3.amazonaws.com/mongodb-mongo-v4.4/stitch-support/ubuntu2004/03d22bb5884e280934d36702136d99a9363fb720/stitch-support-4.4.2-rc0-3-g03d22bb.tgz"
            ;;
            rhel)
                case $DISTRO_VERSION_MAJOR in
                    7)
                        MONGODB_DOWNLOAD_URL="http://downloads.10gen.com/linux/mongodb-linux-x86_64-enterprise-rhel70-4.4.1.tgz"
                        STITCH_ASSISTED_AGG_LIB_URL="https://stitch-artifacts.s3.amazonaws.com/stitch-mongo-libs/stitch_mongo_libs_linux_64_ac073d06065af6e00103a8a3cf64672a3f875bea_20_12_01_19_47_16/libmongo.so"
                        STITCH_SUPPORT_LIB_URL="https://s3.amazonaws.com/stitch-artifacts/stitch-support/stitch-support-rhel-70-4.3.2-721-ge791a2e-patch-5e2a6ad2a4cf473ae2e67b09.tgz"
                    ;;
                    *)
                        echo "Unsupported version of RHEL $DISTRO_VERSION"
                        exit 1
                    ;;
                esac
            ;;
            *)
                echo "Unsupported platform $DISTRO_NAME $DISTRO_VERSION"
                exit 1
            ;;
        esac
    ;;
    *)
        echo "Unsupported platform $(uname -s)"
        exit 1
    ;;
esac

# Allow path to $CURL to be overloaded by an environment variable
CURL=${CURL:=curl}

BASE_PATH=$(cd $(dirname "$0"); pwd)

REALPATH=$BASE_PATH/abspath.sh

if [[ -z $1 ]]; then
    echo "Must specify working directory."
    exit 1
fi
WORK_PATH=$($REALPATH $1)
BAAS_VERSION=$2

[[ -d $WORK_PATH ]] || mkdir -p $WORK_PATH
cd $WORK_PATH

if [[ -f $WORK_PATH/baas_ready ]]; then
    rm $WORK_PATH/baas_ready
fi

echo "Installing node and go to build baas and its dependencies"
echo "Assume node is available"
#export NVM_DIR="$WORK_PATH/.nvm"
#if [ ! -d "$NVM_DIR" ]; then
#    git clone https://github.com/nvm-sh/nvm.git "$NVM_DIR"
#    cd "$NVM_DIR"
#    git checkout `git describe --abbrev=0 --tags --match "v[0-9]*" $(git rev-list --tags --max-count=1)`
#    cd -
#fi
#[[ -s "$NVM_DIR/nvm.sh" ]] && \. "$NVM_DIR/nvm.sh"
#NODE_VERSION=12.16.2
#nvm install --no-progress $NODE_VERSION
#nvm use $NODE_VERSION

[[ -x $WORK_PATH/go/bin/go ]] || ($CURL -sL $GO_URL | tar -xz)
export GOROOT=$WORK_PATH/go
export PATH=$WORK_PATH/go/bin:$PATH

[[ -d baas_dep_binaries ]] || mkdir baas_dep_binaries
export PATH=$WORK_PATH/baas_dep_binaries:$PATH
if [[ ! -x baas_dep_binaries/yq || ! -x baas_dep_binaries/jq ]]; then
    cd baas_dep_binaries
    which yq || ($CURL -LsS $YQ_DOWNLOAD_URL > yq && chmod +x yq)
    which jq || ($CURL -LsS $JQ_DOWNLOAD_URL > jq && chmod +x jq)
    cd -
fi

# If no baas version is specified. Find latest released version
if [[ -z "$BAAS_VERSION" ]]; then
    BAAS_VERSION=$($CURL -LsS "https://realm.mongodb.com/api/private/v1.0/version" | jq -r '.backend.git_hash')
fi

if [[ ! -d $WORK_PATH/baas/.git ]]; then
    git clone git@github.com:10gen/baas.git

fi

cd baas
echo "Checking out baas version $BAAS_VERSION"
git checkout $BAAS_VERSION
cd -

if [[ ! -d $WORK_PATH/baas/etc/dylib/lib ]]; then
    echo "Downloading stitch support library"
    mkdir baas/etc/dylib
    cd baas/etc/dylib
    $CURL -LsS $STITCH_SUPPORT_LIB_URL | tar -xz --strip-components=1
    cd -
fi
export LD_LIBRARY_PATH=$WORK_PATH/baas/etc/dylib/lib

if [[ ! -x $WORK_PATH/baas_dep_binaries/libmongo.so && -n "$STITCH_ASSISTED_AGG_LIB_URL" ]]; then
    echo "Downloading assisted agg library"
    cd $WORK_PATH/baas_dep_binaries
    $CURL -LsS $STITCH_ASSISTED_AGG_LIB_URL > libmongo.so
    chmod 755 libmongo.so
    cd -
fi

if [[ ! -x $WORK_PATH/baas_dep_binaries/assisted_agg && -n "$STITCH_ASSISTED_AGG_URL" ]]; then
    echo "Downloading assisted agg binary"
    cd $WORK_PATH/baas_dep_binaries
    $CURL -LsS $STITCH_ASSISTED_AGG_URL > assisted_agg
    chmod 755 assisted_agg
    cd -
fi

YARN=$WORK_PATH/yarn/bin/yarn
if [[ ! -x $YARN ]]; then
    echo "Getting yarn"
    mkdir yarn && cd yarn
    $CURL -LsS https://s3.amazonaws.com/stitch-artifacts/yarn/latest.tar.gz | tar -xz --strip-components=1
    cd -
    mkdir $WORK_PATH/yarn_cache
fi

if [[ ! -x baas_dep_binaries/transpiler ]]; then
    echo "Building transpiler"
    cd baas/etc/transpiler
    $YARN --non-interactive --silent --cache-folder $WORK_PATH/yarn_cache
    $YARN build --cache-folder $WORK_PATH/yarn_cache --non-interactive --silent
    cd -
    ln -s $(pwd)/baas/etc/transpiler/bin/transpiler baas_dep_binaries/transpiler
fi

if [[ ! -x baas_dep_binaries/stitch-cli ]]; then
    mkdir stitch-cli
    cd stitch-cli
    $CURL -LsS https://github.com/10gen/stitch-cli/archive/v1.9.0.tar.gz | tar -xz --strip-components=1
    go build -o $WORK_PATH/baas_dep_binaries/stitch-cli
    cd -
fi

if [ ! -x $WORK_PATH/mongodb-binaries/bin/mongod ]; then
    echo "Downloading mongodb"
    $CURL -sLS $MONGODB_DOWNLOAD_URL --output mongodb-binaries.tgz

    tar -xzf mongodb-binaries.tgz
    rm mongodb-binaries.tgz
    mv mongodb* mongodb-binaries
    chmod +x ./mongodb-binaries/bin/*
fi

if [[ -d mongodb-dbpath ]]; then
    rm -rf mongodb-dbpath
fi
mkdir mongodb-dbpath

function cleanup() {
    if [[ -f $WORK_PATH/baas_server.pid ]]; then
        PIDS_TO_KILL="$(< $WORK_PATH/baas_server.pid)"
    fi

    if [[ -f $WORK_PATH/mongod.pid ]]; then
        PIDS_TO_KILL="$(< $WORK_PATH/mongod.pid) $PIDS_TO_KILL"
    fi

    if [[ -n "$PIDS_TO_KILL" ]]; then
        echo "Killing $PIDS_TO_KILL"
        kill $PIDS_TO_KILL
        echo "Waiting for processes to exit"
        wait
    fi
}

trap "exit" INT TERM ERR
trap cleanup EXIT

echo "Starting mongodb"
[[ -f $WORK_PATH/mongodb-dbpath/mongod.pid ]] && rm $WORK_PATH/mongodb-path/mongod.pid
./mongodb-binaries/bin/mongod \
    --replSet rs \
    --bind_ip_all \
    --port 26000 \
    --logpath $WORK_PATH/mongodb-dbpath/mongod.log \
    --dbpath $WORK_PATH/mongodb-dbpath/ \
    --pidfilepath $WORK_PATH/mongod.pid &

./mongodb-binaries/bin/mongo \
    --nodb \
    --eval 'assert.soon(function(x){try{var d = new Mongo("localhost:26000"); return true}catch(e){return false}}, "timed out connecting")' \
> /dev/null

echo "Initializing replica set"
./mongodb-binaries/bin/mongo --port 26000 --eval 'rs.initiate()' > /dev/null

cd $WORK_PATH/baas
echo "Adding stitch user"
go run -exec="env LD_LIBRARY_PATH=$LD_LIBRARY_PATH" cmd/auth/user.go \
    addUser \
    -domainID 000000000000000000000000 \
    -mongoURI mongodb://localhost:26000 \
    -salt 'DQOWene1723baqD!_@#'\
    -id "unique_user@domain.com" \
    -password "password"

[[ -d tmp ]] || mkdir tmp
echo "Starting stitch app server"
[[ -f $WORK_PATH/baas_server.pid ]] && rm $WORK_PATH/baas_server.pid
go build -o $WORK_PATH/baas_server cmd/server/main.go
$WORK_PATH/baas_server \
    --configFile=etc/configs/test_config.json 2>&1 > $WORK_PATH/baas_server.log &
echo $! > $WORK_PATH/baas_server.pid
$BASE_PATH/wait_for_baas.sh $WORK_PATH/baas_server.pid
touch $WORK_PATH/baas_ready
echo "Baas server ready"
wait

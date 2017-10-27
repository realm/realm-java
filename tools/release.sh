#!/usr/bin/env bash

# Script to make release on the local machine.
# See https://github.com/realm/realm-wiki/wiki/Java-Release-Checklist for more details.

set -euo pipefail
IFS=$'\n\t'

usage() {
cat <<EOF
Usage: $0 <branch-to-release>
EOF
}

######################################
# Input Validation
######################################

if [ "$#" -eq 0 ] || [ "$#" -gt 1 ] ; then
    usage
    exit 1
fi

######################################
# Variables
######################################

BRANCH_TO_RELEASE="$1"
VERSION=""
REALM_IO_PATH=${REALM_IO_PATH:-}
REALM_JAVA_PATH=$(pwd)

check_adb_device() {
    if ! adb get-state 1>/dev/null 2>&1 ; then
        read -n 1 -s -p -r "Attach a test device or start the emulator then press any key to continue..."
        echo ""
        check_adb_device
    fi
}

check_env() {
    echo "Checking environment..."

    # Try to find s3cmd
    path_to_s3cmd=$(which s3cmd)
    if [[ ! -x "$path_to_s3cmd" ]] ; then
        echo "Cannot find executable file 's3cmd'."
        exit -1
    fi
    if [[ ! -e "$HOME/.s3cfg" ]] ; then
        echo "'$HOME/.s3cfg' cannot be found."
        exit -1
    fi

    # Check BinTray credentials
    if ! grep "bintrayUser=realm" "$HOME/.gradle/gradle.properties" > /dev/null ; then
        echo "'bintrayUser' is not set in the '$HOME/.gradle/gradle.properties'."
        exit -1
    fi

    if ! grep "bintrayKey=.*" "$HOME/.gradle/gradle.properties" > /dev/null; then
        echo "'bintrayKey' is not set in the '$HOME/.gradle/gradle.properties'."
        exit -1
    fi

    # Check gradle params
    if grep buildTargetABIs "$HOME/.gradle/gradle.properties" | grep -v "^#" > /dev/null ; then
        echo "'buildTargetABIs' should be disabled in the '$HOME/.gradle/gradle.properties'."
        exit -1
    fi
    if grep ccachePath "$HOME/.gradle/gradle.properties" | grep -v "^#" > /dev/null ; then
        echo "'ccachePath' should be disabled in the '$HOME/.gradle/gradle.properties'."
        exit -1
    fi
    if grep lcachePath "$HOME/.gradle/gradle.properties" | grep -v "^#" > /dev/null ; then
        echo "'lcachePath' should be disabled in the '$HOME/.gradle/gradle.properties'."
        exit -1
    fi

    if [[ -z ${REALM_IO_PATH} ]] ; then
        REALM_IO_PATH="$(pwd)/../realm.io"
    fi
    if [[ ! -e ${REALM_IO_PATH} ]] ; then
        echo "Please set 'REALM_IO_PATH' to the 'realm.io' repository path to publish javadoc."
        exit -1
    fi
}

prepare_branch() {
    echo "Preparing release branch..."

    git fetch --all
    git checkout releases
    git reset --hard origin/releases
    git clean -xfd
    git submodule update --init --recursive

    # Merge the branch to the releases branch and check the CHANGELOG.md
    if [[ "$BRANCH_TO_RELEASE" != "releases" ]] ; then
        git merge "origin/$BRANCH_TO_RELEASE"
        git submodule update --init --recursive

        while true
        do
            read -r -p "Type the command to edit CHANGELOG.md, default(vim):" editor
            if [ -z "$editor" ] ; then
                editor="vim"
            fi
            "$editor" CHANGELOG.md

            read -r -p "Please merge the unreleased entries in the 'CHANGELOG.md' and then press any key to continue..." _
            if [ "$(grep -c "YYYY-MM-DD" CHANGELOG.md)" -eq 1 ] ; then
                break
            else
                echo "There are more than one or none unreleased entries in the 'CHANGELOG.md'."
            fi
        done
        # CHANGELOG.md is modified.
        if ! git diff-index --quiet HEAD CHANGELOG.md ; then
            git add CHANGELOG.md
            git commit -m "Merge entries in changelog"
        fi
    fi

    if ! grep -q "SNAPSHOT" version.txt ; then
        echo "'version.txt' doesn't contain 'SNAPSHOT'."
        exit -1
    fi

    version_in_changelog=$(head -1 CHANGELOG.md | grep -o "[0-9]*\.[0-9]*\.[0-9]*")
    VERSION=$(grep -o "[0-9]*\.[0-9]*\.[0-9]*" version.txt)
    if [[ "${VERSION}" != "${version_in_changelog}" ]] ; then
        echo "'version.txt' doens't match the entry in 'CHANGELOG.md'. ${VERSION} vs ${version_in_changelog}."
        exit -1
    fi

    # Check if tag exists in remote
    if git ls-remote --tags origin | grep "v${VERSION}" > /dev/null ; then
        echo "Tag 'v${VERSION}' exists in remote!"
        exit -1
    fi
    if git tag | grep "v${VERSION}" > /dev/null ; then
        git tag -d "v${VERSION}"
    fi

    # Update date in change log
    cur_date=$(date "+%F")
    sed "1 s/YYYY-MM-DD/${cur_date}/" CHANGELOG.md > CHANGELOG.md.tmp && mv CHANGELOG.md.tmp CHANGELOG.md
    git add CHANGELOG.md
    git commit -m "Update changelog date"

    # This will create 2 new commits to change the version.txt. The top one is the next release version + SNAPSHOT.
    ./gradlew release
    # Checkout the one with current version number.
    git checkout HEAD~1
}

build() {
    echo "Building..."

    ./gradlew assemble

    echo "Verifying examples..."

    check_adb_device

    # Verify examples
    (cd examples && ./gradlew clean uninstallAll && ./gradlew monkeyDebug)
}

upload_to_bintray() {
    echo "Uploading artifacts to Bintray..."
    # Upload to bintray
    ./gradlew bintrayUpload

    echo "Done."
    echo "1. Log into BinTray(https://bintray.com) with the Realm account;"
    echo "2. Goto https://bintray.com/realm/maven and check if there are 16 artifacts to publish."
    echo "3. Press 'Publish'."
    while true
    do
        read -r -p "Have you published 16 artifacts on Bintray? Type 'Yes' to continue... " input

        case "$input" in
            [yY][eE][sS])
                break
                ;;
        esac
    done
}

publish_distribution() {
    echo "Publishing distribution package..."

    # Create distribution package
    ./gradlew distributionPackage
    pushd build/outputs/distribution
    unzip "realm-java-${VERSION}.zip"

    # Test
    check_adb_device
    pushd examples/
    ./gradlew clean uninstallAll
    ./gradlew monkeyRelease
    popd
    popd
    ./gradlew distribute
}

push_release() {
    echo "Pushing releases branch to origin..."

    # Push branch & tag
    git checkout releases
     
     # Don't push to releases branch if we are doing a beta release.
    if [[ ! "$VERSION" =~ [a-zA-Z] ]] ; then
        git push origin releases
        git push origin "v${VERSION}"
    else
        echo "Non-final release. Release was not pushed to Github. Remember to remove commits on `releases` branch manually."
    fi
}

publish_javadoc() {
    echo "Publishing javadoc..."
    cd "${REALM_IO_PATH}"
    git fetch --all
    branch_name=publish_java_doc/${VERSION}
    git checkout origin/master -b "${branch_name}"

    while true
    do
        read -r -p "Type 'Yes' to clean uncommitted files in 'source/en/docs/java'... " input

        case "$input" in
            [yY][eE][sS])
                break
                ;;
        esac
    done
    git clean -xfd ./source/en/docs/java/
    bundle update
    bundle exec rake generate:java_docs[$VERSION]
    cp -R "${REALM_JAVA_PATH}"/realm/realm-library/build/docs/javadoc/* ./source/en/docs/java/latest/api/
    bundle exec rake generate:inject_ga_latest_java_api
    git add ./source/en/docs/java/
    git commit -m "Release realm-java doc ${VERSION}"
    git push origin "${branch_name}"
    path_to_hub=$(which hub)
    if [[ ! -x "$path_to_hub" ]] ; then
        echo "'hub' cannot be found in the executable path."
        echo "Please create a pull request manually in realm.io repo with branch ${branch_name}."
        exit -1
    else
        hub pull-request
        echo "A pull request has been created for branch ${branch_name}."
    fi
}

check_env
prepare_branch
build
upload_to_bintray
publish_distribution
push_release
publish_javadoc

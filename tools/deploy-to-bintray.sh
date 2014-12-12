#!/bin/bash

set -e

# Make sure we are in root folder
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${script_dir}/.."

if [[ $# -ne 1 ]]; then
    echo "Usage: sh ./deploy-to-bintray.sh <version>"
    echo "Version could eg be \"0.74.1\""
    exit 1
fi

version="$1"

if ! [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
	echo "The provided version is not in a valid format. eg 0.74.1"
	exit 1
fi

version_file_contents=$(<version.txt)
if [[ $version_file_contents != $version ]]; then
	echo "The provided version argument does not match the contents of the version.txt file"
	exit 1
fi

length=${#version}
beginning=$(head -c ${length} changelog.txt)
if [[ $beginning != $version ]]; then
	echo "The provided version argument does not match the beginning of the changelog.txt file"
	exit 1
fi

version_checker_file="realm-annotations-processor/src/main/java/io/realm/processor/RealmVersionChecker.java"

if ! grep -q "$version" "$version_checker_file"; then
	echo "The version checker file does not contain the provided version"
fi

#./gradlew realm:bintrayUpload
#!/usr/bin/env bash

# This script will attempt to manually unroll a stack trace using the unstripped jni libs that are available on S3.
# It will do so using ndk-stack, so read https://developer.android.com/ndk/guides/ndk-stack.html first
#
# The location of ndk-stack will be infered from the ndk.dir property in `<root>/realm/local.properties`.
#
# Usage: > sh unroll_stacktrace.sh <version> <abi> <stacktrace_file>
# Example: > sh unroll_stacktrace.sh 5.0.0 armeabi-v7a ./dump.txt
#

set -euo pipefail
IFS=$'\n\t'

usage() {
cat <<EOF
Usage: $0 <flavor> <version> <abi> <stacktrace>
 - version: version number on Bintray
 - abi: armeabi, armeabi-v7a, arm64-v8a, x86, x86_64, mips
 - flavor: base, objectServer
 - stacktrace: absolute or relative path to file with dump information

Example: $0 base 5.0.0 armeabi-v7a ./dump.txt
EOF
}

######################################
# Input Validation
######################################

if [ "$#" -eq 0 ] || [ "$#" -lt 4 ] ; then
    usage
    exit 1
fi

HERE=$(pwd)
REALM_JAVA_TOOLS_DIR=$(dirname "$0")
FLAVOR="$1"
VERSION="$2"
ABI="$3"
STACKTRACE="$HERE/$4"
NDK_STACK=""
STRIPPED_LIBS_DIR=""

find_ndkstack() {
	PROPS_FILE="$REALM_JAVA_TOOLS_DIR/../realm/local.properties"
	if [ ! -f "$PROPS_FILE" ]; then
    	echo "$PROPS_FILE not found! NDK location cannot be determined"
    	exit 1
	fi
	NDK_STACK=$(grep "ndk.dir" "$PROPS_FILE" | cut -d = -f2)/ndk-stack
}

download_and_unzip_stripped_libs() {
	# Define location for unstripped libs. 
	# Use the standard REALM_CORE if defined, otherwise treat it as a temporary file.
	CACHED_LIBS_DIR="$REALM_CORE_DOWNLOAD_DIR"
	if [[ -z "${REALM_CORE_DOWNLOAD_DIR}" ]]; then
  		CACHED_LIBS_DIR="/tmp"
	fi

	# Check if we already have the unstripped libs downloaded
	STRIPPED_LIBS_FILE="$CACHED_LIBS_DIR/realm-java-jni-libs-unstripped-$VERSION.zip"
	if [ ! -f "$STRIPPED_LIBS_FILE" ]; then
    	echo "$STRIPPED_LIBS_FILE not found! Downloading from S3"
		STRIPPED_LIBS_DOWNLOAD_LOCATION="https://static.realm.io/downloads/java/realm-java-jni-libs-unstripped-$VERSION.zip"
		curl -o "$STRIPPED_LIBS_FILE" "$STRIPPED_LIBS_DOWNLOAD_LOCATION" 
	fi

	# Exact files if needed
	STRIPPED_LIBS_DIR="$CACHED_LIBS_DIR/realm-java-jni-libs-unstripped-$VERSION"
	if [ ! -d "$STRIPPED_LIBS_DIR" ]; then
		echo "Extracting archive file with unstripped libraries"
		unzip "$STRIPPED_LIBS_FILE" -d "$STRIPPED_LIBS_DIR"
	fi
}

unroll_stacktrace() {
	DIR="$STRIPPED_LIBS_DIR/$FLAVOR/$ABI"
	if [ ! -d "$DIR" ]; then
		echo "Directory containing .so file could not be found: ${DIR}"
		exit 1
	fi
	$NDK_STACK -sym "$DIR" -dump "$STACKTRACE"
}

echo "Unrolling $STACKTRACE from Realm Java $VERSION ($FLAVOR) using ABI $ABI"
find_ndkstack
download_and_unzip_stripped_libs
unroll_stacktrace

#!/usr/bin/python3

#
# This script allows to extract build ids from binaries published in our realm java S3 bucket.
#
# The user can specify an individual version or a set, that will be outputed as a table.
#
# The process can be slow and resource consuming, as it needs to download each release, extract it
# to finally run the readelf tool.
#

import os
import sys
import subprocess
import urllib.request
import zipfile
import argparse

READELF_TOOL_PATH = '/toolchains/llvm/prebuilt/darwin-x86_64/bin/x86_64-linux-android-readelf'
FIND_NDK_COMMAND = 'find $ANDROID_HOME/ndk -name ".*" -prune -maxdepth 1 -o -print | sort -rV | head -n 1'
NDK_PATH = subprocess.check_output(FIND_NDK_COMMAND, shell=True).decode('ascii').strip()
BUILD_ID_EXTRACT_COMMMAND = NDK_PATH + READELF_TOOL_PATH + ' -n {0} | grep "Build ID" | cut -d ":" -f2'

FLAVORS = ['base', 'objectServer']
ARCHS = ['arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64', 'mips']

ZIPS_PATH = os.getcwd() + "/downloads/"
LIBS_PATH = os.getcwd() + "/libs/"


def file_exists(path):
    f = None
    try:
        f = open(path)
        return True
    except IOError:
        return False
    finally:
        if f:
            f.close()


def download_file(url, path):
    urllib.request.urlretrieve(url, path)


def generator(major_range, minor_range, patch_range):
    for major in major_range:
        for minor in minor_range:
            for patch in patch_range:
                yield f"{major}.{minor}.{patch}"


def create_dirs(path):
    try:
        os.mkdir(path)
    except OSError:
        pass
    return path


def unzip(zip_file, version):
    print(f"Unziping v{version}", file=sys.stderr)

    path = LIBS_PATH + f"{version}"
    create_dirs(path)
    with zipfile.ZipFile(zip_file, "r") as zip_ref:
        zip_ref.extractall(path)


def format_table_line(build_id, version, flavor, arch):
    return f"{build_id.ljust(41)} {version.ljust(10)} {flavor.ljust(14)} {arch}"


def extract_build_ids(ids, version):
    print(f"Processing v{version}", file=sys.stderr)

    for flavor in FLAVORS:
        for arch in ARCHS:
            so_path = f"{LIBS_PATH}/{version}/{flavor}/{arch}/librealm-jni.so"
            if file_exists(so_path):
                build_id = subprocess.check_output(BUILD_ID_EXTRACT_COMMMAND.format(so_path), shell=True).strip().decode('ascii')
                ids.append(format_table_line(build_id, version, flavor, arch))


def download(zip_path, version):
    if not file_exists(zip_path):
        print(f"Downloading {version}", file=sys.stderr)
        download_file(f"https://static.realm.io/downloads/java/realm-java-jni-libs-unstripped-{version}.zip", zip_path)


def parse_range(version):
    if version is None:
        print("⚠️ Please define major minor and patch, see --help ")
        exit()
    if '-' in version:
        version_range = version.split('-')
        return range(int(version_range[0]), int(version_range[1]) + 1)
    return [int(version)]

if __name__ == "__main__":
    create_dirs(ZIPS_PATH)
    create_dirs(LIBS_PATH)

    parser = argparse.ArgumentParser(description='Extract build ids out from S3.')
    parser.add_argument('major', nargs='?', help='specific version: 1 or range: 1-3')
    parser.add_argument('minor', nargs='?', help='specific version: 1 or range: 1-3')
    parser.add_argument('patch', nargs='?', help='specific version: 1 or range: 1-3')
    args = parser.parse_args()

    major_range = parse_range(args.major)
    minor_range = parse_range(args.minor)
    patch_range = parse_range(args.patch)

    build_ids = []

    print("-- This process can take some minutes ⏰ --", file=sys.stderr)

    for version in generator(major_range, minor_range, patch_range):
        zip_path = ZIPS_PATH + f"{version}.zip"
        try:
            download(zip_path, version)
            unzip(zip_path, version)
            extract_build_ids(build_ids, version)
        except Exception:
            print(f"Skipping v{version}, it does not exist in S3", file=sys.stderr)

    print("-- Done 🚀 --\n\n", file=sys.stderr)

    print(format_table_line("Build id", "Version", "Flavor", "Arch"))

    max_length = max([len(x) for x in build_ids])
    print(''.ljust(max_length, '-'))

    for build_id in build_ids:
        print(build_id)

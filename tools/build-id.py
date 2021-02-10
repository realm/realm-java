#!/usr/bin/python3

import os
import subprocess
import urllib.request
import zipfile
import argparse

FLAVORS = ['base', 'objectServer']
ARCHS = ['arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64']
BUILD_ID_EXTRACT_COMMMAND = '/Users/clemente.tort/Library/Android/sdk/ndk/21.0.6113669/toolchains/llvm/prebuilt/darwin-x86_64/bin/x86_64-linux-android-readelf -n {0} | grep "Build ID" | cut -d ":" -f2'

def download_file(url, path):
    urllib.request.urlretrieve(url, path)


def generator(major_range, minor_range, patch_range):
    for major in major_range:
        for minor in minor_range:
            for patch in patch_range:
                yield f"{major}.{minor}.{patch}"


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


def get_path(path):
    try:
        os.mkdir(path)
    except OSError:
        pass
    return path


def unzip(zip_file, path):
    with zipfile.ZipFile(zip_file,"r") as zip_ref:
        zip_ref.extractall(path)

def format_table_line(build_id, version, flavor, arch):
    return f"{build_id.ljust(41)} : {version.ljust(10)} {flavor.ljust(14)} {arch}"


def extract_build_ids(path, version):
    for flavor in FLAVORS:
        for arch in ARCHS:
            so_path = f"{path}{version}/{flavor}/{arch}/librealm-jni.so"
            build_id = subprocess.check_output(BUILD_ID_EXTRACT_COMMMAND.format(so_path), shell=True).strip().decode('ascii')
            yield format_table_line(build_id, version, flavor, arch)

def parse_range(version):
    if version is None:
        print("⚠️ Please define major minor and patch, see --help ")
        exit()

    if '-' in version:
        version_range = version.split('-')
        return range(int(version_range[0]), int(version_range[1]) + 1)
    
    return [int(version)]

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Extract build ids out from S3.')
    parser.add_argument('major', nargs='?', help='specific version: 1 or range: 1-3')
    parser.add_argument('minor', nargs='?', help='specific version: 1 or range: 1-3')
    parser.add_argument('patch', nargs='?', help='specific version: 1 or range: 1-3')

    args = parser.parse_args()

    major_range = parse_range(args.major)
    minor_range = parse_range(args.minor)
    patch_range = parse_range(args.patch)
    
    zips_path = get_path(os.getcwd() + "/downloads/")
    libs_path = get_path(os.getcwd() + "/libs/")
    build_ids = []

    print("-- This process can take some minutes ⏰ --")
    for version in generator(major_range, minor_range, patch_range):
        zip_path = zips_path + f"{version}.zip"

        try:
            if not file_exists(zip_path):
                print(f"Downloading {version}")
                download_file(f"https://static.realm.io/downloads/java/realm-java-jni-libs-unstripped-{version}.zip", zip_path)
            
            print(f"Unziping {version}")
            lib_path = get_path(libs_path + f"{version}")
            unzip(zip_path, lib_path)
            
            print(f"Processing {version}")
            build_ids = build_ids + [ x for x in extract_build_ids(libs_path, version) ]
        except Exception:
            print(f"Skipping {version}, it does not exist in S3")
    print("-- Done 🚀 --\n\n")
    print(format_table_line("Build id", "Version", "Flavor", "Arch"))

    for build_id in build_ids:
        print(build_id)

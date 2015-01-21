#!/bin/bash

if [ -z "$1" ]; then
    echo "You must supply a version number"
    exit 1
fi

realm_java_version="$1"
realm_java_version_file="realm/src/main/java/io/realm/internal/Version.java"
realm_java_ver_major="$(echo "$realm_java_version" | cut -f1 -d.)" || exit 1
realm_java_ver_minor="$(echo "$realm_java_version" | cut -f2 -d.)" || exit 1
realm_java_ver_patch="$(echo "$realm_java_version" | cut -f3 -d.)" || exit 1

# update Version.java
printf ",s/int REALM_JAVA_MAJOR .*/int REALM_JAVA_MAJOR = $realm_java_ver_major\;/\nw\nq" | ed -s "$realm_java_version_file" || exit 1
printf ",s/int REALM_JAVA_MINOR .*/int REALM_JAVA_MINOR = $realm_java_ver_minor\;/\nw\nq" | ed -s "$realm_java_version_file" || exit 1
printf ",s/int REALM_JAVA_PATCH .*/int REALM_JAVA_PATCH = $realm_java_ver_patch\;/\nw\nq" | ed -s "$realm_java_version_file" || exit 1

# update annotations processor
realm_apt_file="realm-annotations-processor/src/main/java/io/realm/processor/RealmVersionChecker.java"
printf ",s/String REALM_VERSION .*/String REALM_VERSION = \"$realm_java_version\"\;/\nw\nq" | ed -s "$realm_apt_file" || exit 1

# update version.text
echo -n "$realm_java_ver_major.$realm_java_ver_minor.$realm_java_ver_patch" > version.text || exit 1

echo "Remember to update JNI headers by running realm-jni/generate-jni-headers.sh"
exit 0

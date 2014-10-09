#!/bin/bash
#
# Generate header file for JNI calls:
# 1. Compile relevant files (and what they depend on)
# 2. Generate the headers
# 3. Remove the class files
# 4. Remove "empty" header files

REALMDIR="../realm/src/main/java/io/realm/"
TMPDIR=$(mktemp -d /tmp/$$.XXXXXX)
JNIDIR="$(pwd)/src"
(cd "$REALMDIR" && javac -d "$TMPDIR" internal/*.java exceptions/*.java)
(cd "$TMPDIR" && javah -jni -classpath "$TMPDIR" -d "$JNIDIR" io.realm.internal.Group io.realm.internal.LinkView io.realm.internal.Row io.realm.internal.SharedGroup io.realm.internal.SubtableSchema io.realm.internal.Table io.realm.internal.TableQuery io.realm.internal.TableView io.realm.internal.Util io.realm.internal.Version)

rm -rf "$TMPDIR"
wc -l "$JNIDIR"/*.h | grep " 13 " | awk '{print $2}' | xargs rm -f

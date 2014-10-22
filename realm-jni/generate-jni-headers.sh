#!/bin/bash
#
# Generate header file for JNI calls

# Setting up
REALMDIR="../realm/src/main/java/io/realm/"
TMPDIR=$(mktemp -d /tmp/$$.XXXXXX)
JNIDIR="$(pwd)/src"

# Compile relevant files (and what they depend on)
(cd "$REALMDIR" && javac -d "$TMPDIR" internal/*.java exceptions/*.java)

# Generate the headers
(cd "$TMPDIR" && javah -jni -classpath "$TMPDIR" -d "$JNIDIR" io.realm.internal.Group io.realm.internal.LinkView io.realm.internal.Row io.realm.internal.SharedGroup io.realm.internal.SubtableSchema io.realm.internal.Table io.realm.internal.TableQuery io.realm.internal.TableView io.realm.internal.Util io.realm.internal.Version)

# Remove the class files
rm -rf "$TMPDIR"

# Remove "empty" header files (they have 13 lines)
wc -l "$JNIDIR"/*.h | grep " 13 " | awk '{print $2}' | xargs rm -f

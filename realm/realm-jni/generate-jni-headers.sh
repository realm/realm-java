#!/bin/bash
#
# Generate header file for JNI calls
#
# Assumption: the .java files have been compiled
# 1. build project using Android Studio (or gradle); unit tests will probably fail due to missing native methods
# 2. run this script
# 3. rebuild project using Android Studio (or gradle)

# Setting up
CLASSDIR="$(pwd)/../realm/build/intermediates/classes/release/"
JNIDIR="$(pwd)/src"

# Generate the headers
(cd "$CLASSDIR" && javah -jni -classpath "$CLASSDIR" -d "$JNIDIR" io.realm.internal.Group io.realm.internal.LinkView io.realm.internal.Row io.realm.internal.SharedGroup io.realm.internal.SubtableSchema io.realm.internal.Table io.realm.internal.TableQuery io.realm.internal.TableView io.realm.internal.Util io.realm.internal.Version)

# Remove "empty" header files (they have 13 lines)
wc -l "$JNIDIR"/*.h | grep " 13 " | awk '{print $2}' | xargs rm -f

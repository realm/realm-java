cd "$(dirname "$0")"
TIGHTDB_JAVA_HOME="$(pwd)"

MODE="$1"
[ $# -gt 0 ] && shift

MAKE="make -j8"
JAVA_INC=include
JAVA_BIN=bin


# Setup OS specific stuff
OS="$(uname -s)" || exit 1
if [ "$OS" = "Darwin" ]; then
    MAKE="$MAKE CC=clang"
    JAVA_INC=Headers
    JAVA_BIN=Commands
fi


# Find 'jni.h', 'java' and 'javac'
if [ -z "$JAVA_HOME" ]; then
    if ! which javac >/dev/null; then
        echo "No JAVA_HOME and no Java compiler in PATH" 1>&2
        exit 1
    fi
    readlink_f()
    {
        local l="$(readlink "$1")"
        if [ -z "$l" -o "$l" = "$1" ]; then
            echo "$1"
            return
        fi
        readlink_f "$l"
    }
    JAVAC=$(readlink_f "$(which javac)")
    if ! echo "$JAVAC" | grep "/$JAVA_BIN/javac"'$' >/dev/null 2>&1; then
        echo "Could not determine JAVA_HOME from path of 'javac' command" 1>&2
        exit 1
    fi
    JAVA_HOME="$(echo "$JAVAC" | sed "s|/$JAVA_BIN/javac$||")"
fi
for x in "$JAVA_INC/jni.h" "$JAVA_BIN/java" "$JAVA_BIN/javac"; do
    if ! [ -f "$JAVA_HOME/$x" ]; then
        echo "No '$x' in '$JAVA_HOME'" 1>&2
        exit 1
    fi
done
JAVA="$JAVA_HOME/$JAVA_BIN/java"
JAVAC="$JAVA_HOME/$JAVA_BIN/javac"
JAVA_VERSION="$(java -version 2>&1 | grep '^java version' | sed 's/.*"\(.*\).*"/\1/')"
JAVA_MAJOR="$(echo "$JAVA_VERSION" | cut -d. -f1)"
JAVA_MINOR="$(echo "$JAVA_VERSION" | cut -d. -f2)"
if [ "$JAVA_MAJOR" -lt 1 -o \( "$JAVA_MAJOR" -eq 1 -a "$JAVA_MINOR" -lt 6 \) ]; then
    echo "Need Java version 1.6 or newer (is '$JAVA_VERSION')" 1>&2
    exit 1
fi



case "$MODE" in

"build")
    # Build libtightdb-jni.so
    cd "$TIGHTDB_JAVA_HOME/tightdb_jni/src" || exit 1
    $MAKE clean || exit 1
    $MAKE EXTRA_CFLAGS="-I\"$JAVA_HOME/$JAVA_INC\"" || exit 1

    # Build tightdb.jar
    cd "$TIGHTDB_JAVA_HOME/src/main/java" || exit 1
    find com/ -type f -name '*.class' -delete || exit 1
    export CLASSPATH=/usr/share/java/commons-io.jar:/usr/share/java/commons-lang.jar:/usr/share/java/freemarker.jar:.
    $JAVAC $(find com/ -type f -name '*.java' | fgrep -v /doc/ | fgrep -v /example/) || exit 1
    jar cf tightdb.jar $(find com/ -type f -name '*.class') || exit 1
    ;;

"test")
    # Build and run test suite
    cd "$TIGHTDB_JAVA_HOME/src/test/java" || exit 1
    find com/ -type f -name '*.class' -delete || exit 1
    test_sources="$(find * -type f -name '*Test.java')"
    test_classes="$(echo "$test_sources" | sed 's/\.java$/.class/')"
    export CLASSPATH="$TIGHTDB_JAVA_HOME/src/main/java/tightdb.jar:$TIGHTDB_JAVA_HOME/tightdb-example/src/main/java:/usr/share/java/testng.jar:/usr/share/java/qdox.jar:/usr/share/java/bsh.jar:."
    $JAVAC $test_sources || exit 1
    $JAVA -Djava.library.path="$TIGHTDB_JAVA_HOME/tightdb_jni/src" org.testng.TestNG -d "$TIGHTDB_JAVA_HOME/test_output" -testclass $test_classes || exit 1
    ;;

"install")
    MAKE="$MAKE"
    if [ "$INSTALL_ROOT" ]; then
        MAKE="$MAKE prefix=\"$INSTALL_ROOT\""
    fi
    $MAKE install || exit 1
    ;;

"copy")
    # Copy to distribution package
    TARGET_DIR="$1"
    if ! [ "$TARGET_DIR" -a -d "$TARGET_DIR" ]; then
        echo "Unspecified or bad target directory '$TARGET_DIR'" 1>&2
        exit 1
    fi
    TEMP_DIR="$(mktemp -d /tmp/temp.XXXX)" || exit 1
    git ls-files -z >"$TEMP_DIR/files" || exit 1
    tar czf "$TEMP_DIR/archive.tar.gz" --null -T "$TEMP_DIR/files" || exit 1
    (cd "$TARGET_DIR" && tar xzf "$TEMP_DIR/archive.tar.gz") || exit 1
    ;;

*)
    echo "Unspecified or bad mode '$MODE'" 1>&2
    exit 1
    ;;

esac

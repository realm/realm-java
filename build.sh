# NOTE: THIS SCRIPT IS SUPPOSED TO RUN IN A POSIX SHELL

cd "$(dirname "$0")"
TIGHTDB_JAVA_HOME="$(pwd)"

MODE="$1"
[ $# -gt 0 ] && shift

MAKE="make -j8"
JAVA_INC="include"
JAVA_BIN="bin"
JNI_LIB_SUFFIX=""
JNI_LIB_INST_DIR="lib/jni" # Absolute or relative to installation prefix


# Setup OS specific stuff
OS="$(uname -s)" || exit 1
if [ "$OS" = "Darwin" ]; then
    MAKE="$MAKE CC=clang"
    JAVA_INC="Headers"
    JAVA_BIN="Commands"
    JNI_LIB_SUFFIX=".jnilib"
    JNI_LIB_INST_DIR="/System/Library/Java/Extensions"
fi



readlink_f()
{
    local LINK TARGET
    LINK="$1"
    if ! TARGET="$(readlink "$LINK")"; then
        printf "%s\n" "$LINK"
        return 0
    fi
    readlink_f "$TARGET"
}

remove_suffix()
{
    local string suffix match_x
    string="$1"
    suffix="$2"
    match_x="$(printf "%s" "$string" | tail -c "${#suffix}" && echo x)" || return 1
    if [ "$match_x" != "${suffix}x" ]; then
        printf "%s\n" "$string"
        return 0
    fi
    printf "%s" "$string" | sed "s/.\{${#suffix}\}\$//" || return 1
    echo
    return 0
}

# Find 'jni.h', 'java' and 'javac'
find_java()
{
    if [ -z "$JAVA_HOME" ]; then
        if ! JAVAC="$(which javac)"; then
            echo "No JAVA_HOME and no Java compiler in PATH" 1>&2
            return 1
        fi
        JAVAC="$(readlink_f "$JAVAC")" || return 1
        JAVA_HOME="$(remove_suffix "$JAVAC" "/$JAVA_BIN/javac")" || return 1
        if [ "$JAVA_HOME" = "$JAVAC" ]; then
            echo "Could not determine JAVA_HOME from path of 'javac' command" 1>&2
            return 1
        fi
    fi
    for x in "$JAVA_INC/jni.h" "$JAVA_BIN/java" "$JAVA_BIN/javac"; do
        if ! [ -f "$JAVA_HOME/$x" ]; then
            echo "No '$x' in '$JAVA_HOME'" 1>&2
            return 1
        fi
    done
    JAVA="$JAVA_HOME/$JAVA_BIN/java"
    JAVAC="$JAVA_HOME/$JAVA_BIN/javac"
    JAVA_VERSION="$(java -version 2>&1 | grep '^java version' | sed 's/.*"\(.*\).*"/\1/')"
    JAVA_MAJOR="$(printf "%s\n" "$JAVA_VERSION" | cut -d. -f1)" || return 1
    JAVA_MINOR="$(printf "%s\n" "$JAVA_VERSION" | cut -d. -f2)" || return 1
    if ! [ "$JAVA_MAJOR" -gt 1 -o "$JAVA_MAJOR" -eq 1 -a "$JAVA_MINOR" -ge 6 ] 2>/dev/null; then
        echo "Need Java version 1.6 or newer (is '$JAVA_VERSION')" 1>&2
        return 1
    fi
}



case "$MODE" in

    "clean")
        cd "$TIGHTDB_JAVA_HOME/tightdb_jni/src" || exit 1
        $MAKE clean || exit 1
        cd "$TIGHTDB_JAVA_HOME/src/main" || exit 1
        find java/ -type f -name '*.class' -delete || exit 1
        rm -f tightdb.jar || exit 1
        exit 0
        ;;

    "build")
        find_java || exit 1

        # Build libtightdb-jni.so
        cd "$TIGHTDB_JAVA_HOME/tightdb_jni/src" || exit 1
        $MAKE EXTRA_CFLAGS="-I$JAVA_HOME/$JAVA_INC" || exit 1
        if [ "$JNI_LIB_SUFFIX" ]; then
            ln -s "libtightdb-jni.so" "libtightdb-jni$JNI_LIB_SUFFIX"
        fi

        # Build tightdb.jar
        cd "$TIGHTDB_JAVA_HOME/src/main" || exit 1
        DEPENDENCIES="/usr/share/java/commons-io.jar /usr/share/java/commons-lang.jar /usr/share/java/freemarker.jar"
        CLASSPATH="$(printf "%s\n" "$DEPENDENCIES" | sed 's/  */:/g'):java" || exit 1
        export CLASSPATH
        # FIXME: Must run ResourceGenerator to produce java/com/tightdb/generator/Templates.java
        SOURCES="$(find java/ -type f -name '*.java' | grep -v /doc/ | grep -v /example/ | grep -v /test/)" || exit 1
        $JAVAC $SOURCES || exit 1
        CLASSES="$(cd java && find * -type f -name '*.class')" || exit 1
        TEMP_DIR="$(mktemp -d /tmp/tightdb.java.build.XXXX)" || exit 1
        MANIFEST="$TEMP_DIR/MANIFEST.MF"
        echo "Class-Path: $DEPENDENCIES" >>"$MANIFEST"
        jar cfm tightdb.jar "$MANIFEST" -C resources META-INF || exit 1
        (cd java && jar uf ../tightdb.jar $CLASSES) || exit 1
        jar i tightdb.jar || exit 1
        exit 0
        ;;

    "test")
        find_java || exit 1

        # Build and run test suite
        cd "$TIGHTDB_JAVA_HOME/src/test" || exit 1
        TEMP_DIR="$(mktemp -d /tmp/tightdb.java.test.XXXX)" || exit 1
        mkdir "$TEMP_DIR/out" || exit 1
        mkdir "$TEMP_DIR/gen" || exit 1
        export CLASSPATH="$TIGHTDB_JAVA_HOME/src/main/tightdb.jar:/usr/share/java/testng.jar:/usr/share/java/qdox.jar:/usr/share/java/bsh.jar:$TEMP_DIR/gen:."
        (cd java && $JAVAC -d "$TEMP_DIR/out" -s "$TEMP_DIR/gen" com/tightdb/test/TestModel.java) || exit 1
        SOURCES="$(cd java && find * -type f -name '*Test.java')" || exit 1
        CLASSES="$(printf "%s\n" "$SOURCES" | sed 's/\.java$/.class/')" || exit 1
        (cd java && $JAVAC -d "$TEMP_DIR/out" -s "$TEMP_DIR/gen" $SOURCES) || exit 1
        (cd "$TEMP_DIR/out" && $JAVA -Djava.library.path="$TIGHTDB_JAVA_HOME/tightdb_jni/src" org.testng.TestNG -d "$TIGHTDB_JAVA_HOME/test_output" -testclass $CLASSES) || exit 1
        exit 0
        ;;

    "install")
        PREFIX="$1"
        PREFIX_WAS_SPECIFIED="$PREFIX"
        if [ -z "$PREFIX" ]; then
            PREFIX="/usr/local"
        fi
        (cd "$TIGHTDB_JAVA_HOME/tightdb_jni/src" && $MAKE prefix="$PREFIX" install) || exit 1
        if [ "$JNI_LIB_SUFFIX" -a "$JNI_LIB_SUFFIX" != ".so" -o "$JNI_LIB_INST_DIR" -a "$JNI_LIB_INST_DIR" != "lib" ]; then
            SUFFIX="${JNI_LIB_SUFFIX:-.so}"
            INST_DIR="${JNI_LIB_INST_DIR:-lib}"
            if [ "$PREFIX_WAS_SPECIFIED" ]; then
                if printf "%s\n" "$INST_DIR" |grep '^/' >/dev/null; then
                    INST_DIR="lib"
                fi
            fi
            if ! printf "%s\n" "$INST_DIR" |grep '^/' >/dev/null; then
                INST_DIR="$PREFIX/$INST_DIR"
            fi
            if [ "$INST_DIR" = "$PREFIX/lib" ]; then
                (cd "$INST_DIR" && ln -f -s "libtightdb-jni.so" "libtightdb-jni$SUFFIX") || exit 1
            else
                install -d "$INST_DIR" || exit 1
                (cd "$INST_DIR" && ln -f -s "$PREFIX/lib/libtightdb-jni.so" "libtightdb-jni$SUFFIX") || exit 1
            fi
        fi
        install -d "$PREFIX/share/java" || exit 1
        install -m 644 "src/main/tightdb.jar" "$PREFIX/share/java" || exit 1
        # FIXME: See http://developer.apple.com/library/mac/#qa/qa1170/_index.html
        exit 0
        ;;

    "test-installed")
        PREFIX="$1"
        find_java || exit 1
        if [ "$PREFIX" ]; then
            JAVA="$JAVA -Djava.library.path=$PREFIX/lib"
        else
            PREFIX="/usr/local"
        fi
        cd "$TIGHTDB_JAVA_HOME/test-installed" || exit 1
        TEMP_DIR="$(mktemp -d /tmp/tightdb.java.test-installed.XXXX)" || exit 1
        export CLASSPATH="$PREFIX/share/java/tightdb.jar:."
        $JAVAC -d "$TEMP_DIR" -s "$TEMP_DIR" java/my/app/Test.java || exit 1
        (cd "$TEMP_DIR" && $JAVA my.app.Test) || exit 1
        exit 0
        ;;

    "dist-copy")
        # Copy to distribution package
        TARGET_DIR="$1"
        if ! [ "$TARGET_DIR" -a -d "$TARGET_DIR" ]; then
            echo "Unspecified or bad target directory '$TARGET_DIR'" 1>&2
            exit 1
        fi
        TEMP_DIR="$(mktemp -d /tmp/tightdb.java.copy.XXXX)" || exit 1
        git ls-files -z >"$TEMP_DIR/files" || exit 1
        tar czf "$TEMP_DIR/archive.tar.gz" --null -T "$TEMP_DIR/files" || exit 1
        (cd "$TARGET_DIR" && tar xzf "$TEMP_DIR/archive.tar.gz") || exit 1
        exit 0
        ;;

    *)
        echo "Unspecified or bad mode '$MODE'" 1>&2
        echo "Available modes are: clean build test install test-installed" 1>&2
        echo "As well as: dist-copy" 1>&2
        exit 1
        ;;

esac

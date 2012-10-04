# NOTE: THIS SCRIPT IS SUPPOSED TO RUN IN A POSIX SHELL

cd "$(dirname "$0")"
TIGHTDB_JAVA_HOME="$(pwd)"

MODE="$1"
[ $# -gt 0 ] && shift


DEP_JARS="/usr/share/java/commons-io.jar /usr/share/java/commons-lang.jar /usr/share/java/freemarker.jar"


# Setup OS specific stuff
OS="$(uname)" || exit 1
ARCH="$(uname -m)" || exit 1
STAT_FORMAT_SWITCH="-c"
NUM_PROCESSORS=""
if [ "$OS" = "Darwin" ]; then
    if [ "$CC" = "" ] && which clang >/dev/null; then
        export CC=clang
    fi
    STAT_FORMAT_SWITCH="-f"
    NUM_PROCESSORS="$(sysctl -n hw.ncpu)" || exit 1
else
    if [ -r /proc/cpuinfo ]; then
        NUM_PROCESSORS="$(cat /proc/cpuinfo | grep -E 'processor[[:space:]]*:' | wc -l)" || exit 1
    fi
fi
if [ "$NUM_PROCESSORS" ]; then
    export MAKEFLAGS="-j$NUM_PROCESSORS"
fi
USE_LIB64=""
IS_REDHAT_DERIVATIVE=""
if [ -e /etc/redhat-release ] || grep -q "Amazon" /etc/system-release 2>/dev/null; then
    IS_REDHAT_DERIVATIVE="1"
fi
if [ "$IS_REDHAT_DERIVATIVE" -o -e /etc/SuSE-release ]; then
    if [ "$ARCH" = "x86_64" -o "$ARCH" = "ia64" ]; then
        USE_LIB64="1"
    fi
fi
JNI_SUFFIX=".so"
JNI_LIBDIR="lib" # Absolute, or relative to installation prefix
JAVA_INC="include"
JAVA_BIN="bin"
if [ "$OS" = "Darwin" ]; then
    JNI_LIBDIR="/System/Library/Java/Extensions"
    JNI_SUFFIX=".jnilib"
    JAVA_INC="Headers"
    JAVA_BIN="Commands"
elif [ "$USE_LIB64" ]; then
    JNI_LIBDIR="/usr/lib64/jni"
else
    JNI_LIBDIR="/usr/lib/jni"
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

same_path_target()
{
    local A B A_ID B_ID
    A="$1"
    B="$2"
    if [ -e "$A" -a -e "$B" ]; then
        A_ID="$(stat -L "$STAT_FORMAT_SWITCH%d:%i" "$A")" || exit 1
        B_ID="$(stat -L "$STAT_FORMAT_SWITCH%d:%i" "$B")" || exit 1
        if [ "$A_ID" = "$B_ID" ]; then
            return 0
        fi
    fi
    return 1
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
    if [ -z "$JAVA_HOME" -o \! -e "$JAVA_HOME/$JAVA_BIN/javac" ]; then
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
        make clean || exit 1
        cd "$TIGHTDB_JAVA_HOME/src/main" || exit 1
        find java/ -type f -name '*.class' -delete || exit 1
        rm -f tightdb.jar || exit 1
        exit 0
        ;;

    "build")
        find_java || exit 1

        # Build libtightdb-jni.so
        cd "$TIGHTDB_JAVA_HOME/tightdb_jni/src" || exit 1
        TIGHTDB_ENABLE_FAT_BINARIES="1" make EXTRA_CFLAGS="-I$JAVA_HOME/$JAVA_INC -I$JAVA_HOME/$JAVA_INC/linux" || exit 1
        if [ "$JNI_SUFFIX" != ".so" ]; then
            ln -s "libtightdb-jni.so" "libtightdb-jni$JNI_SUFFIX"
        fi

        # Build tightdb.jar
        cd "$TIGHTDB_JAVA_HOME/src/main" || exit 1
        (cd java && $JAVAC                com/tightdb/*.java  com/tightdb/lib/*.java)  || exit 1
        (cd java && jar cf ../tightdb.jar com/tightdb/*.class com/tightdb/lib/*.class) || exit 1
        jar i tightdb.jar || exit 1

        # Build tightdb-devkit.jar
        CLASSPATH="$(printf "%s\n" "$DEP_JARS" | sed 's/  */:/g'):../tightdb.jar" || exit 1
        export CLASSPATH
        # FIXME: Must run ResourceGenerator to produce java/com/tightdb/generator/Templates.java
        TEMP_DIR="$(mktemp -d /tmp/tightdb.java.build.XXXX)" || exit 1
        MANIFEST="$TEMP_DIR/MANIFEST.MF"
        echo "Class-Path: tightdb.jar $DEP_JARS" >>"$MANIFEST"
        jar cfm tightdb-devkit.jar "$MANIFEST" -C resources META-INF || exit 1
        (cd java && $JAVAC                       com/tightdb/generator/*.java)  || exit 1
        (cd java && jar uf ../tightdb-devkit.jar com/tightdb/generator/*.class) || exit 1
        jar i tightdb-devkit.jar || exit 1

        # Setup links to libraries and JARs to make the examples work
        mkdir -p "$TIGHTDB_JAVA_HOME/examples/lib" || exit 1
        cd "$TIGHTDB_JAVA_HOME/examples/lib" || exit 1
        for x in "../../src/main/tightdb.jar" "../../src/main/tightdb-devkit.jar" "../../tightdb_jni/src/libtightdb-jni$JNI_SUFFIX" "../../../tightdb/src/tightdb/libtightdb.so"; do
            ln -s -f "$x" || exit 1
        done
        exit 0
        ;;

    "test")
        find_java || exit 1

        # Build and run test suite
        cd "$TIGHTDB_JAVA_HOME/src/test" || exit 1
        TEMP_DIR="$(mktemp -d /tmp/tightdb.java.test.XXXX)" || exit 1
        mkdir "$TEMP_DIR/out" || exit 1
        mkdir "$TEMP_DIR/gen" || exit 1
        export CLASSPATH="$TIGHTDB_JAVA_HOME/src/main/tightdb-devkit.jar:/usr/share/java/testng.jar:/usr/share/java/qdox.jar:/usr/share/java/bsh.jar:$TEMP_DIR/gen:."
        # Newer versions of testng.jar (probably >= 6) require beust-jcommander.jar
        if [ -e "/usr/share/java/beust-jcommander.jar" ]; then
            CLASSPATH="$CLASSPATH:/usr/share/java/beust-jcommander.jar"
        fi
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
        if [ "$USE_LIB64" ]; then
            LIBDIR="$PREFIX/lib64"
        else
            LIBDIR="$PREFIX/lib"
        fi
        make -C "$TIGHTDB_JAVA_HOME/tightdb_jni/src" prefix="$PREFIX" libdir="$LIBDIR" install || exit 1
        INST_DIR="$JNI_LIBDIR"
        if [ "$PREFIX_WAS_SPECIFIED" ]; then
            if printf "%s\n" "$INST_DIR" | grep -q '^/'; then
                INST_DIR="lib"
            fi
        fi
        if ! printf "%s\n" "$INST_DIR" | grep -q '^/'; then
            INST_DIR="$PREFIX/$INST_DIR"
        fi
        if ! same_path_target "$INST_DIR" "$PREFIX/lib"; then
            install -d "$INST_DIR" || exit 1
            (cd "$INST_DIR" && ln -f -s "$PREFIX/lib/libtightdb-jni.so" "libtightdb-jni$JNI_SUFFIX") || exit 1
        elif [ "$JNI_SUFFIX" != ".so" ]; then
            (cd "$INST_DIR" && ln -f -s "libtightdb-jni.so" "libtightdb-jni$JNI_SUFFIX") || exit 1
        fi
        install -d "$PREFIX/share/java" || exit 1
        install -m 644 "src/main/tightdb.jar" "src/main/tightdb-devkit.jar" "$PREFIX/share/java" || exit 1
        # FIXME: See http://developer.apple.com/library/mac/#qa/qa1170/_index.html
        exit 0
        ;;

    "test-installed")
        PREFIX="$1"
        find_java || exit 1
        if [ "$PREFIX" ]; then
            if [ "$USE_LIB64" ]; then
                LIBDIR="$PREFIX/lib64"
            else
                LIBDIR="$PREFIX/lib"
            fi
            JAVA="$JAVA -Djava.library.path=$LIBDIR"
        else
            PREFIX="/usr/local"
        fi
        cd "$TIGHTDB_JAVA_HOME/test-installed" || exit 1
        TEMP_DIR="$(mktemp -d /tmp/tightdb.java.test-installed.XXXX)" || exit 1
        export CLASSPATH="$PREFIX/share/java/tightdb-devkit.jar:."
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

        # Copy dependecy JARs
        mkdir -p "$TARGET_DIR/dep_jars" || exit 1
        cp $DEP_JARS "$TARGET_DIR/dep_jars/" || exit 1
        exit 0
        ;;

    "dist-remarks")
        echo "To build the java language binding, the following JAR files must have"
        echo "been installed on you system:"
        echo
        for x in $DEP_JARS; do
            echo "  $x"
        done
        echo
        echo "If you do not have them already, you may want to copy them from"
        echo "tightdb_java2/dep_jars/."
        echo
        echo "A simple example is provided in tightdb_java2/examples/intro-example"
        echo "to help you get started."
        exit 0
        ;;

    *)
        echo "Unspecified or bad mode '$MODE'" 1>&2
        echo "Available modes are: clean build test install test-installed" 1>&2
        echo "As well as: dist-copy dist-remarks" 1>&2
        exit 1
        ;;

esac

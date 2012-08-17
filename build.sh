cd "$(dirname "$0")"
TIGHTDB_JAVA_HOME="$(pwd)"

MAKE="make -j8"

# Setup OS specific stuff
OS="$(uname -s)" || exit 1
if [ "$OS" = "Darwin" ]; then
    MAKE="$MAKE CC=gcc"
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
    if ! echo "$JAVAC" | grep '/bin/javac$' >/dev/null 2>&1; then
        echo "Could not determine JAVA_HOME from path of 'javac' command" 1>&2
        exit 1
    fi
    JAVA_HOME="$(echo "$JAVAC" | sed 's|/bin/javac$||')"
fi
for x in include/jni.h bin/java bin/javac; do
    if ! [ -f "$JAVA_HOME/$x" ]; then
        echo "No '$x' in '$JAVA_HOME'" 1>&2
        exit 1
    fi
done
JAVA="$JAVA_HOME/bin/java"
JAVAC="$JAVA_HOME/bin/javac"
JAVA_VERSION="$(java -version 2>&1 | grep '^java version' | sed 's/.*"\(.*\).*"/\1/')"
JAVA_MAJOR="$(echo "$JAVA_VERSION" | cut -d. -f1)"
JAVA_MINOR="$(echo "$JAVA_VERSION" | cut -d. -f2)"
if [ "$JAVA_MAJOR" -lt 1 -o \( "$JAVA_MAJOR" -eq 1 -a "$JAVA_MINOR" -lt 6 \) ]; then
    echo "Need Java version 1.6 or newer (is '$JAVA_VERSION')" 1>&2
    exit 1
fi



# Build libtightdb-jni.so
cd "$TIGHTDB_JAVA_HOME/tightdb_jni/src" || exit 1
$MAKE clean || exit 1
$MAKE EXTRA_CFLAGS="-I\"$JAVA_HOME/include\"" || exit 1

# Build tightdb.jar
cd "$TIGHTDB_JAVA_HOME/src/main/java" || exit 1
find com/ -type f -name '*.class' -delete || exit 1
export CLASSPATH=/usr/share/java/commons-io.jar:/usr/share/java/commons-lang.jar:/usr/share/java/freemarker.jar:.
$JAVAC $(find com/ -type f -name '*.java' | fgrep -v /doc/ | fgrep -v /example/) || exit 1
jar cf tightdb.jar $(find com/ -type f -name '*.class') || exit 1

# Build and run test suite
cd "$TIGHTDB_JAVA_HOME/src/test/java" || exit 1
find com/ -type f -name '*.class' -delete || exit 1
test_sources="$(find * -type f -name '*Test.java')"
test_classes="$(echo "$test_sources" | sed 's/\.java$/.class/')"
export CLASSPATH="$TIGHTDB_JAVA_HOME/src/main/java/tightdb.jar:$TIGHTDB_JAVA_HOME/tightdb-example/src/main/java:/usr/share/java/testng.jar:/usr/share/java/qdox.jar:/usr/share/java/bsh.jar:."
$JAVAC $test_sources || exit 1
$JAVA -Djava.library.path="$TIGHTDB_JAVA_HOME/tightdb_jni/src" org.testng.TestNG -d "$TIGHTDB_JAVA_HOME/test_output" -testclass $test_classes || exit 1

JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 # Ununtu 12.04 64-bit
TIGHTDB_JAVA_HOME="$HOME/tightdb_java2"


# Build libtightdb-jni.so
cd "$TIGHTDB_JAVA_HOME/tightdb_jni/src" || exit 1
make clean || exit 1
make -j8 EXTRA_CFLAGS="-I\"$JAVA_HOME/include\" -DTIGHTDB_ENABLE_REPLICATION" || exit 1

# Build tightdb.jar
cd "$TIGHTDB_JAVA_HOME/src/main/java" || exit 1
find com/ -type f -name '*.class' -delete || exit 1
export CLASSPATH=/usr/share/java/commons-io.jar:/usr/share/java/commons-lang.jar:/usr/share/java/freemarker.jar:.
javac $(find com/ -type f -name '*.java' | fgrep -v /doc/ | fgrep -v /example/) || exit 1
jar cf tightdb.jar $(find com/ -type f -name '*.class') || exit 1

# Build and run replication example
cd "$TIGHTDB_JAVA_HOME/tightdb-example/src/main/java" || exit 1
find com/ -type f -name '*.class' -delete || exit 1
export CLASSPATH="$TIGHTDB_JAVA_HOME/src/main/java/tightdb.jar:."
javac com/tightdb/example/ReplicationExample.java com/tightdb/example/generated/*.java || exit 1
java -Djava.library.path="$TIGHTDB_JAVA_HOME/tightdb_jni/src" com.tightdb.example.ReplicationExample --database-file /tmp/replication.tdb || exit 1

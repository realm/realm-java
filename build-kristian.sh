JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 # Ununtu 12.04 64-bit
TIGHTDB_HOME=$HOME/tightdb_bmunkholm
TIGHTDB_JAVA2_HOME=$HOME/tightdb_java2


# Build libtightdb.so
cd $TIGHTDB_HOME
make clean || exit 1
make || exit 1

# Build libtightdb-jni.so
cd $TIGHTDB_JAVA2_HOME/tightdb_jni/src || exit 1
make clean || exit 1
make EXTRA_CFLAGS="-I$JAVA_HOME/include -I$TIGHTDB_HOME/src" || exit 1

# Combine libtightdb-jni.so and libtightdb.so (The Java code should be changed such that this is not necessary!)
cd $TIGHTDB_JAVA2_HOME/tightdb_jni/src || exit 1
g++ -shared -fPIC -DPIC -O3 -msse4.2 -DUSE_SSE -pthread *.dyn.o $TIGHTDB_HOME/src/tightdb/*.dyn.o -o libtightdb-jni.so || exit 1

# Build tightdb.jar
cd $TIGHTDB_JAVA2_HOME/src/main/java || exit 1
find com/ -type f -name '*.class' -delete || exit 1
javac -cp /usr/share/java/commons-io.jar:/usr/share/java/velocity.jar:/usr/share/java/commons-lang.jar:/usr/share/java/freemarker.jar $(find com/ -type f -name '*.java' | fgrep -v /example/) || exit 1
jar cf tightdb.jar $(find com/ -type f -name '*.class') || exit 1

# Build and run example
cd $TIGHTDB_JAVA2_HOME/tightdb-example/src/main/java || exit 1
find com/ -type f -name '*.class' -delete || exit 1
javac -cp $TIGHTDB_JAVA2_HOME/src/main/java/tightdb.jar com/tightdb/example/Example.java com/tightdb/example/generated/*.java || exit 1
java -Djava.library.path=$TIGHTDB_JAVA2_HOME/tightdb_jni/src -cp $TIGHTDB_JAVA2_HOME/src/main/java/tightdb.jar:. com.tightdb.example.Example

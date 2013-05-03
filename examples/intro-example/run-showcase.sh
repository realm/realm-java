#!/bin/sh
# environment
JAVA_LIB_PATH=$(pwd)/../lib
CLASSPATH=$(pwd)/bin:$(pwd)/../lib/tightdb.jar:$(pwd)/../lib-sqlite/sqlite4java.jar
mkdir -p $(pwd)/bin
mkdir -p $(pwd)/generated

# compile
JAVAC=$(which javac)
if [ -n "$JAVAC" ]; then
    echo "compiling ..."
    FILES="Showcase.java"
    for f in $FILES; do
        javac -classpath $CLASSPATH \
            -sourcepath $(pwd)/src \
            -target 1.6 -g:none \
            -processor com.tightdb.generator.CodeGenProcessor \
            -processorpath ../lib/tightdb-devkit.jar \
            -s generated -proc:only -source 1.6 \
            $(pwd)/src/com/tightdb/examples/showcase/$f > /dev/null 2>&1
    done

    for f in $FILES; do
        javac -classpath $CLASSPATH \
            -d $(pwd)/bin \
            -classpath $CLASSPATH \
            -sourcepath $(pwd)/src:$(pwd)/generated \
            -target 1.6 \
            -g:source,line,vars \
            -source 1.6 \
            $(pwd)/src/com/tightdb/examples/showcase/$f > /dev/null 2>&1
    done
fi

echo "running ..."
java -Djava.library.path=$JAVA_LIB_PATH \
    -classpath $CLASSPATH \
    com.tightdb.examples.showcase.Showcase
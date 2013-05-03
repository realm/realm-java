#!/bin/sh

# environment
JAVA_LIB_PATH="../../lib"
export CLASSPATH="bin:../../lib/tightdb.jar:../lib-sqlite/sqlite4java.jar"
mkdir -p $(pwd)/bin
mkdir -p $(pwd)/generated

# compile
JAVAC=$(which javac)
if [ -n "$JAVAC" ]; then
    echo "compiling ..."
    FILES="com/tightdb/examples/tutorial/tutorial.java"
    for f in $FILES; do
        javac \
            -sourcepath "src" \
            -target 1.6 -g:none \
            -processor com.tightdb.generator.CodeGenProcessor \
            -processorpath "../../lib/tightdb-devkit.jar" \
            -s "generated" -proc:only -source 1.6 \
            "src/$f" > /dev/null 2>&1
    done

    for f in $FILES; do
        javac \
            -d "bin" \
            -classpath $CLASSPATH \
            -sourcepath "src:generated" \
            -target 1.6 \
            -g:source,line,vars \
            -source 1.6 \
            "src/$f" > /dev/null 2>&1
    done
fi

echo "running ..."
java -Djava.library.path=$JAVA_LIB_PATH \
    com.tightdb.examples.tutorial.tutorial

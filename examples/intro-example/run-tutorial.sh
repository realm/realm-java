#!/bin/sh

# environment

# FIXME: This one is not working. The idea was that this test should be able to run without requiring installation of core library and Java language binding. It looks like the problem is in out custom class loader. Due to this problem, the example fails unless installation has been performed first.
JAVA_LIB_PATH="$(pwd)/../lib"


export CLASSPATH="bin:../lib/tightdb.jar:../lib-sqlite/sqlite4java.jar"
mkdir "bin"
mkdir "generated"

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
            -processorpath "../lib/tightdb-devkit.jar" \
            -s "generated" -proc:only -source 1.6 \
            "src/$f"
    done

    for f in $FILES; do
        javac \
            -d "bin" \
            -sourcepath "src:generated" \
            -target 1.6 \
            -source 1.6 \
            "src/$f"
    done
fi

echo "running ..."
java -Djava.library.path=$JAVA_LIB_PATH \
    com.tightdb.examples.tutorial.tutorial

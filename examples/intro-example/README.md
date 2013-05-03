Examples
========
Within this folder (and subfolders) you find a number of examples on
how to use TightDB.

For each example, a simple shell script is provided. These shell
scripts compile the example (if you have a Java compiler installed),
set up the Java runtime environment and execute the example. If you
make any changes to an example, the script will recompile for you.

If you have a complete Java development kit installed, you can build
all example by executing the command:

    ant


Tutorial
--------
This is the source code you find in the tutorial. You can run the
tutorial example by executing the following command:

    ./run-tutorial.sh

The source code is found in the folder `src/com/tightdb/examples/tutorial`.

Alternatively, you can execute the following command if you have `ant`
installed:

    ant tutorial


Showcase
--------
To demonstrate how to use the Java interface of TightDB, you can take
at look at this example. To run it, please execute

    ./run-showcase.sh

The source code is found in the folder `src/com/tightdb/examples/showcase`.

If you have `ant` installed, you can also execute the example using
the command:

    ant showcase


Performance
-----------
You can chooce between many databases and data structures. In this
small and unscientific benchmark, we compare the following four
offerings:

* TightDB
* SQLite
* Java arrays

The benchmark runs as you execute the following command:

    ./run-performance

The source code is found in the folder `src/com/tightdb/examples/performance`.

If you have `ant` installed, you can also run the benchmark by
executing the following command:

    ant performance


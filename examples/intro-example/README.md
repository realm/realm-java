Examples
========
Within this folder (and subfolders) you find a number of examples on
how to use TightDB.

For each example, a simple shell script is provided. These shell
scripts compile the example (if you have a Java compiler installed),
set up the Java runtime environment and execute the example. If you
make any changes to an example, the script will recompile for you.

If you have a complete Java development kit installed, you can build
all examples by executing the command:

        ant


Tutorial
--------
This is the source code you find in the tutorial on www.tightdb.com.
You can run the tutorial example by executing one of the following commands:

        ./run_tutorial.sh
        ant tutorial

The source code is found in the folder `src/com/tightdb/examples/tutorial`.


Showcase
--------
To demonstrate how to use the Java interface of TightDB, you can take
at look at this example. To run it, please execute one of the following commands:

        ./run_showcase.sh
        ant showcase

The source code is found in the folder `src/com/tightdb/examples/showcase`.


Quick benchmark
---------------
To demonstrate how to use the Java interface of TightDB, you can take
at look at this example. To run it, please execute one of the following commands:

        ./run_quickbenchmark.sh
        ant quickbenchmark

The source code is found in the folder `src/com/tightdb/examples/quickbenchmark`.


Performance
-----------
This is a variant of he above benchmark which also measures SQLite.
It is structured for easily adding more databases.
In this small and unscientific benchmark, we compare the following:

* TightDB
* SQLite
* Java arrays

The benchmark runs as you execute one of the following commands:

        ./run_performance
        ant performance

The source code is found in the folder `src/com/tightdb/examples/performance`.

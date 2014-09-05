Setting up a project
====================
For instructions on setting up: Ant, Maven, Eclipse or IntelliJ IDEA,
please consult our online documentation:
http://www.tightdb.com/documentation/Java_misc/1/Tutorial/


Examples
========
Within this folder (and subfolders) you find a number of examples on
how to use TightDB.

If you have a complete Java development kit installed, you can build
all examples by executing the command:

        ant

If you copy any TightDB related jar file, you must update `build.xml`
to be able to compile and run the examples. In particular, you must
update `libpath` and `java.library.path`.


Tutorial
--------
This is the source code you find in the tutorial on www.tightdb.com.
You can run the tutorial example by executing the following command:

        ant tutorial

The source code is found in the folder `src/com/tightdb/examples/tutorial`.


Showcase
--------
To demonstrate how to use the Java interface of TightDB, you can take
at look at this example. To run it, please execute the following command:

        ant showcase

The source code is found in the folder `src/com/tightdb/examples/showcase`.


Quick benchmark
---------------
This is a short unscientific benchmark. It will do some simple benchmarking
of TightDB against Java ArrayList and HashMap.

To run it, please execute the following command:

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

The benchmark runs as you execute the following command:

        ant performance

The source code is found in the folder `src/com/tightdb/examples/performance`.

Examples
========
Within this folder (and subfolders) you find a number of examples on
how to use TightDB. The examples must be build using the command:

    ant


Tutorial
--------
This is the source code you find in the tutorial. You can run the
tutorial example by executing the following command:

    ant tutorial


Showcase
--------
To demonstrate how to use the Java interface of TightDB, you can take
at look at this example. To run it, please execute

    ant showcase


Performance
-----------
You can chooce between many databases and data structures. In this
small and unscientific benchmark, we compare the following four
offerings:

* TightDB
* SQLite
* Java arrays
* OrientDB

In order to run this example, you must install OrientDB 1.3 and create
a databased called _petshop_ under user name _admin_ and password
_admin_. The benchmark runs as you execute the following command:

    ant performance


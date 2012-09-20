Java
====

This is the Java interface to TightDB.

After you have installed below dependencies, you can proceed to build and install TightDB, as described below.


Dependencies
------------

The TightDB core library must have been installed.

### Ubuntu 12.04

#### Build essentials:

    sudo apt-get install build-essential
    sudo apt-get install openjdk-7-jre openjdk-7-jdk

#### Dependencies for building the JNI library:

    sudo apt-get install libproc-dev

#### Dependencies for building the java library:

    sudo apt-get install libcommons-io-java
    sudo apt-get install libcommons-lang-java
    sudo apt-get install libfreemarker-java

#### Dependencies for test suite:

    sudo apt-get install testng
    sudo apt-get install libqdox-java
    sudo apt-get install bsh

### OS X 10.8

    Install Java
    Install libcommons-io-java
    Install libcommons-lang-java
    Install libfreemarker-java


Building and installing
-----------------------

    sh build.sh clean
    sh build.sh build
    sudo sh build.sh install
    sh build.sh test-intalled


Examples
--------
    
    You can try the TightDB with Java in the examples provided in:
    ./examples. Please consult the README file in that directory.
    

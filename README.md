Java
====

This is the Java interface to TightDB.


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


Notes
-----

This project contains both the TightDB library and some examples, too.

Description of packages:
*  com.tightdb - core JNI API classes and minimal test application for it (TableTest)
*  com.tightdb.example - hand-written examples (Example, ManualWorkingExample and WorkingExample) of usage of the API
*  com.tightdb.example.generated - hand-written examples of how the generated code should look like (based on ManualWorkingExample)
*  com.tightdb.generated - automatically generated files from the annotated classes in WorkingExample
*  com.tightdb.generator - code generation logic (related to JAnnocessor and customize.vm in src/main/resources)
*  com.tightdb.lib - the TightDB Java library what wraps around the JNI API, the generated code extends these classes
*  org.jannocessor.config - code generation configuration (JAnnocessor-related package and class names by convention)

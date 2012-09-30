Java
====

This is the Java language binding for TightDB.

After you have installed the dependencies listed below, you can
proceed to build and install the language binding.


Dependencies
------------

The TightDB core library must have been installed.

### Ubuntu 12.04

    #   Build essentials:
    sudo apt-get install build-essential
    #   Java:
    sudo apt-get install openjdk-7-jre openjdk-7-jdk
    #   Dependencies for building the JNI library:
    sudo apt-get install libproc-dev
    #   Dependencies for building the java library:
    sudo apt-get install libcommons-io-java
    sudo apt-get install libcommons-lang-java
    sudo apt-get install libfreemarker-java
    #   Dependencies for test suite:
    sudo apt-get install testng
    sudo apt-get install libqdox-java
    sudo apt-get install bsh

### Fedora 17

    #   Build essentials:
    sudo yum install gcc gcc-c++
    #   Java:
    sudo yum install java-1.7.0-openjdk-devel
    #   Dependencies for building the JNI library:
    sudo yum install procps-devel
    #   Dependencies for building the java library:
    sudo yum install apache-commons-io
    sudo yum install apache-commons-lang
    sudo yum install freemarker
    #   Dependencies for test suite:
    sudo yum install testng
    sudo yum install qdox
    sudo yum install bsh

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


Java
====

This is the TightDB language binding for Java.

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
    #   For building the JNI library if TIGHTDB_ENABLE_MEM_USAGE is specified:
    sudo apt-get install libproc-dev
    #   Dependencies for building the java library:
    sudo apt-get install libcommons-io-java
    sudo apt-get install libcommons-lang-java
    sudo apt-get install libfreemarker-java
    #   Dependencies for test suite:
    sudo apt-get install testng
    sudo apt-get install ant
    sudo apt-get install libqdox-java
    sudo apt-get install bsh

### Fedora 17

    #   Build essentials:
    sudo yum install gcc gcc-c++
    #   Java:
    sudo yum install java-1.7.0-openjdk-devel
    #   For building the JNI library if TIGHTDB_ENABLE_MEM_USAGE is specified:
    sudo yum install procps-devel
    #   Dependencies for building the java library:
    sudo yum install apache-commons-io
    sudo yum install apache-commons-lang
    sudo yum install freemarker
    #   Dependencies for test suite:
    sudo yum install testng
    sudo yum install ant
    sudo yum install qdox
    sudo yum install bsh

### OS X 10.8

    Install Java
    #   Dependencies for building the java library:
    Install /usr/share/java/commons-io.jar (from Apache: http://projects.apache.org/projects/commons_io.html)
    Install /usr/share/java/commons-lang.jar (from Apache: http://projects.apache.org/projects/commons_lang.html)
    Install /usr/share/java/freemarker.jar (http://freemarker.sourceforge.net/)
    #   Dependencies for test suite:
    Install /usr/share/java/testng.jar (http://testng.org/doc/download.html)
    Install /usr/share/java/qdox.jar (http://qdox.codehaus.org/download.html)
    Install /usr/share/java/bsh.jar (http://www.beanshell.org/download.html)


Building and installing
-----------------------

    sh build.sh clean
    sh build.sh build
    sudo sh build.sh install
    sh build.sh test-intalled


Configuration
-------------

To use a nondefault compiler, or a compiler in a nondefault location,
set the environment variable `CC` before calling `sh build.sh build`,
as in the following example:

    CC=clang sh build.sh build

There are also a number of environment variables that serve to enable
or disable special features during building:

Set `TIGHTDB_ENABLE_MEM_USAGE` to a nonempty value to enable
reporting of memory usage.


Examples
--------

You can try the TightDB with Java in the examples provided in:
./examples/intro-example. Please consult the README.md file in that
directory.


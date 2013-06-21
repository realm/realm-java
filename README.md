Java
====

This README file explains how to build and install the TightDB
language binding for Java. It assumes that the TightDB core library
has already been installed.

After building, and before installing, it is possible to run a number
of bundeled examples. If you are interested, see
examples/intro-example/README.md.


Prerequisites
-------------

To build this language binding, you need version 1.6 or newer of the
Java Development Kit (JDK).

You also need the "Commons IO" and "Commons Lang" Java libraries from
Apache Commons (http://commons.apache.org), as well as the
"Freemarker" Java library (http://freemarker.sourceforge.net).

Additionally, you need the standard set of build tools. This includes
a C/C++ compiler and GNU make. TightDB is thoroughly tested with both
GCC and Clang. It is known to work with GCC 4.2 and newer, as well as
with Clang 3.0 and newer.

To run the test suite you need TestNG (http://testng.org) as well as
its dependencies.

The following is a suggestion of how to install the prerequisites on
each of our major platforms:

### Ubuntu 10.04

    sudo apt-get install build-essential openjdk-6-jre openjdk-6-jdk
    sudo apt-get install libcommons-io-java libcommons-lang-java libfreemarker-java
    sudo apt-get install testng libqdox-java bsh

### Ubuntu 12.04 and 13.04

    sudo apt-get install build-essential openjdk-7-jre openjdk-7-jdk
    sudo apt-get install libcommons-io-java libcommons-lang-java libfreemarker-java
    sudo apt-get install testng libqdox-java bsh

### Fedora 17 and 18

    sudo yum install gcc gcc-c++ java-1.7.0-openjdk-devel
    sudo yum install apache-commons-io apache-commons-lang freemarker
    sudo yum install testng qdox bsh

### Amazon Linux 2012.09

    sudo yum install gcc gcc-c++ java-1.7.0-openjdk-devel
    sudo yum install apache-commons-io apache-commons-lang
    sudo yum install qdox bsh

Java libraries Freemarker and TestNG are not currently available in
the Yum repository, but they can be downloaded and installed manually
from the respective wesites. To make this process a little easier, we
have bundled the relevant libraries with the TightDB Java extension in
the `prerequisite_jars` subdirectory, but please note that TightDB
makes no guarantees, and has no responsibility with respect to the
fitness of the bundled libraries on any target system.

With that said, here are the commands that will install the Freemarker
JAR on your system (please be carefull about overwriting existing
files):

    sudo install -d /usr/local/share/java
    sudo install -m 644 prerequisite_jars/freemarker.jar /usr/local/share/java

If you intend to run the optional test-suite, you also need this one:

    sudo install -m 644 prerequisite_jars/testng.jar /usr/local/share/java

### Mac OS X 10.7 and 10.8

On Mac OS X, the build procedure uses Clang as the C/C++
compiler. Clang comes with Xcode, so install Xcode if it is not
already installed. If you have a version that preceeds 4.2, we
recommend that you upgrade. This will ensure that the Clang version is
at least 3.0. Run the following command in the command prompt to see
if you have Xcode installed, and, if so, what version it is:

    xcodebuild -version

Make sure you also install "Command line tools" found under the
preferences pane "Downloads" in Xcode.

Run the following command on the command prompt to see if Java is
already ínstalled:

    java -version

If Java is not already installed, this command will initiate the
installation procedure.

The prerequisite Java libraries (JAR's) can be downloaded and
installed individually from the respective wesites. To make this
process a little easier, we have bundled the relevant libraries with
the TightDB Java extension in the `prerequisite_jars` subdirectory,
but please note that TightDB makes no guarantees, and has no
responsibility with respect to the fitness of the bundled libraries on
any target system.

With that said, here are the commands that will install the bundeled
JAR's on your system (please be carefull about overwriting existing
files):

    sudo install -d /usr/local/share/java
    sudo install -m 644 prerequisite_jars/commons-lang.jar /usr/local/share/java
    sudo install -m 644 prerequisite_jars/commons-io.jar /usr/local/share/java
    sudo install -m 644 prerequisite_jars/freemarker.jar /usr/local/share/java

If you intend to run the optional test-suite, you also need these:

    sudo install -m 644 prerequisite_jars/testng.jar /usr/local/share/java
    sudo install -m 644 prerequisite_jars/qdox.jar /usr/local/share/java
    sudo install -m 644 prerequisite_jars/bsh.jar /usr/local/share/java


Building, testing, and installing
---------------------------------

    sh build.sh config
    sh build.sh clean
    sh build.sh build
    sh build.sh test
    sudo sh build.sh install
    sh build.sh test-intalled

This procedure will install the following two native JNI libraries in
an appropriate system directory (see "Configuration" below):

    libtightdb-jni.so
    libtightdb-jni-dbg.so

Note: '.so' is replaced by '.jnilib' on OS X.

It will also install the following two Java libraries:

    /usr/local/share/java/tightdb.jar
    /usr/local/share/java/tightdb-devkit.jar

The 'devkit' variant includes an annotation processor and should be
used when compiling your application, as in the following example:

    CLASSPATH=/path/to/tightdb-devkit.jar:. javac foo/MyApp.java

To run your application you only need `tightd.jar`:

    CLASSPATH=/path/to/tightdb.jar:. java foo.MyApp

You can instruct `tightdb.jar` to use the debug version of the native
library by setting `TIGHTDB_JAVA_DEBUG` to a nonempty value, as in the
following example:

    TIGHTDB_JAVA_DEBUG=1 CLASSPATH=/path/to/tightdb.jar:. java foo.MyApp

After building, you might want to see exactly what will be installed,
without actually instyalling anything. This can be done as follows:

    DESTDIR=/tmp/check sh build.sh install && find /tmp/check -type f


Configuration
-------------

It is possible to install into a non-default location by running the
following command before building and installing:

    sh build.sh config [PREFIX]

Here, `PREFIX` is the installation prefix (e.g. `/usr/local`). If it
is not specified, it will be determined automatically to match the
installed JDK.

By default, the language binding is built for the JDK that is
associated with the Java compiler found by `which javac`. To build for
a JDK in a different location, set the environment variable
`JAVA_HOME` before calling `sh build.sh config`, as in the following
example:

    JAVA_HOME=/opt/jdk-1.7 sh build.sh config

To use a nondefault compiler, or a compiler in a nondefault location,
set the environment variable `CC` before calling `sh build.sh build`,
as in the following example:

    CC=clang sh build.sh build

There are also a number of environment variables that serve to enable
or disable special features during building:

Set `TIGHTDB_ENABLE_MEM_USAGE` to a nonempty value to enable
reporting of memory usage.


Packaging
---------

It is possible to create Debian packages (`.deb`) by running the
following command:

    dpkg-buildpackage -rfakeroot

The packages will be signed by the maintainer's signature.

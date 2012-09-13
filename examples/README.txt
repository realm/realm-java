TightDB example instructions
============================

Below are instructions for testing the TightDB example application in Java using Eclipse or 
further down using a command-line.

If you wish to integrate TightDB in an _existing_ application using Eclipse, please read "eclipse_setup.txt".

If you wish to integrate TightDB in an _existing_ application without using Eclipse:
  - for Maven-based projects please read "maven_setup.txt",
  - for Ant-based projects please read "ant_setup.txt".


==================
Short introduction
==================

TightDB for Java comes packaged into several files:
- a native, binary library (.dll, .so, etc.), that needs to be copied into the "lib" folder,
- tightdb.jar - the TightDB run-time that uses the native library,
- tightdb-devkit.jar - TightDB's tool for annotation-driven code generation at compile-time (NOT needed at run-time).

        
=====================================
Buiding TightDB example using Eclipse
=====================================

1. Prerequisites:
-----------------
1.1. Installation of Eclipse
   - Download and install Eclipse from www.eclipse.org

1.2. Installation of JDK7 (or JDK6)
   - Download and install JDK7 from oracle.com


2. Installation of TightDB example
----------------------------------
2.1. Download and extract TightDB example project into a workspace directory, e.g. "./my-workspace/tightdb-example".

2.2. Open eclipse and select the workspace directory, e.g. "./my-workspace".

2.3. Import Example:
- Select "File -> Import". 
- Choose "Maven -> Existing Maven Project" (or "General -> Existing Projects into Workspace" if you don't have Maven installed).
- Select root directory: Browse for the "tightdb-example" folder and select it.


3. Installation of the TightDB JARs (required only if you use Maven)
-------------------------------------------------------------------------------
- Navigate to the "tightdb-example" folder:
cd <path-to-the-example>/tightdb-example

- Install the tightdb-devkit.jar and tightdb-devkit.jar libraries into the local Maven repository:
mvn install:install-file -Dfile=lib/tightdb.jar -DgroupId=com.tightdb -DartifactId=tightdb -Dversion=1.1.0 -Dpackaging=JAR
mvn install:install-file -Dfile=lib/tightdb-devkit.jar -DgroupId=com.tightdb -DartifactId=tightdb-devkit -Dversion=1.1.0 -Dpackaging=JAR


4. Running the application
--------------------------

4.1. Run the application
   - Select "Run -> Run"

and output from the application will be shown in the Console window.


5. Generating TightDB classes
-----------------------------
You can now change the example as you wish. If you change or add TightDB tables, 
you need to regenerate the type-safe classes that TightDB provides based on the @Table specifications.

5.1. All you have to do is save your files, and the generated files will be updated.

5.2. Sometimes you might need to refresh the project (F5) if the most recent changes aren't reflected.
The generated classes will be available with full auto-completion etc.

You can now run your application again.




   ---- OO ----



================================================================
Buiding TightDB example from the command line using Maven
================================================================

- Navigate to the "tightdb-example" folder:
cd <path-to-the-example>/tightdb-example

- Install the tightdb-devkit.jar and tightdb-devkit.jar libraries into the local Maven repository:
mvn install:install-file -Dfile=lib/tightdb.jar -DgroupId=com.tightdb -DartifactId=tightdb -Dversion=1.1.0 -Dpackaging=JAR
mvn install:install-file -Dfile=lib/tightdb-devkit.jar -DgroupId=com.tightdb -DartifactId=tightdb-devkit -Dversion=1.1.0 -Dpackaging=JAR

- Clean:
mvn clean

- Generate TightDB sources:
mvn process-sources

- Build:
mvn clean package

- Run (for Linux and Mac OS X):
java -Djava.library.path=lib -cp lib/tightdb.jar:target/classes com.tightdb.example.Example

- Run (for Windows):
java -Djava.library.path=lib -cp lib/tightdb.jar;target/classes com.tightdb.example.Example


================================================================
Buiding TightDB example from the command line using Ant
================================================================

Generate TightDB classes
------------------------

- Navigate to the "tightdb-example" folder:
cd <path-to-the-example>/tightdb-example

- Clean:
ant clean

- Generate TightDB sources:
ant generate-sources

- Build:
ant build

- Run (for Linux and Mac OS X):
java -Djava.library.path=lib -cp lib/tightdb.jar:target/classes com.tightdb.example.Example

- Run (for Windows):
java -Djava.library.path=lib -cp lib/tightdb.jar;target/classes com.tightdb.example.Example



FAQ:
====
Q1: I get the following error when generating classes as describe in step 4) above:
"[ERROR] JVM is not suitable for processing annotation! ToolProvider.getSystemJavaCompiler() is null."
A1: Install and Configure the project to use JDK6 or JDK7.



Feedback is more than welcome!
Don't hesitate to contact us at support@tightdb.com.

Enjoy!
/The TightDB team

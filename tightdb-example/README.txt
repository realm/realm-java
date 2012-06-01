Tightdb example instructions
============================

Below are instructions for testing the Tightdb example application in Java using Eclipse or 
further down using a commandline.

If you wish to integrate Tightdb in an _existing_ application that uses Maven, please read "maven_setup.txt".
If you wish to integrate Tightdb in an _existing_ application that uses Ant, please read "ant_setup.txt".


=====================================
Buiding Tightdb example using Eclipse
=====================================


1. Prerequisites:
-----------------
1. Installation of Eclipse
   - Download and install Eclipse from www.eclipse.org

2. Installation of JDK7 (or JDK6)
   - Download and install JDK7 from oracle.com


2. Installation of tightdb example
----------------------------------
1. Download and extract tightdb example to a directory e.g. "./tightdb".

2. Create new workspace directory "./tightdb/workspace". 
   - Open eclipse and select above directory

3. Import Example:
   Select "File->Import". 
   Choose "Maven->Existing Maven Project". Root directory: Browse for "tightdb-example"

   Note: Errors may occur at this point about missing Maven plugins. 
   Those will be resolved after generating and installing additional plugins later on 
   (see 4. Generating tightdb classes)


3. Running the application
--------------------------

1. Run the application
   - Select "Run -> Run"

and output from the application will be shown in the Console window.
 

4. Generating tightdb classes
-----------------------------
You can now change the example and as you wish. If you change or add Tightdb Tables, 
you need to regenerate the typesafe classes that Tightdb provides based on the Table specifications.


1. Save files
   - Save all edited source files containing @Table definitions

2. Right-click tightdb-example -> Run As -> Maven generate-sources.
   This will generate custom classes for your tables in "com.tightdb.generated".
   The first time this is done, Eclipse will installadditional plugins if not already present.

3. Refresh the project (F5) and the generated classes are available with full autocompletion etc.

You can now run your application again.




   ---- OO ----




================================================================
Buiding Tightdb example from the command line using Maven or Ant
================================================================

Generate tightdb classes
------------------------
ant generate-sources
- or - 
mvn generate-sources

Build
-----
ant build
- or -
mvn compile

Clean
-----
ant clean
- or -
mvn clean

Run
---
java -Djava.library.path=. -cp lib/tightdb.jar:target/classes com.tightdb.example.Example



FAQ:
====
Q1: I get the following error when generating classes as describe in step 4) above:
"[ERROR] JVM is not suitable for processing annotation! ToolProvider.getSystemJavaCompiler() is null."
A1: Install and Configure the project to use JDK6 or JDK7.




Feedback is more than welcome!
Don't hesitate to contact us at support@tightdb.com.

Enjoy!
/The Tightdb team


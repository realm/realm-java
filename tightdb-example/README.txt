Tightdb example instructions
============================

Below are instructions for testing the Tightdb example application in Java.


1. Prerequisites:
-----------------
1. Installation of Eclipse
   - Download and install Eclipse from www.eclipse.org

2. Installation of JDK6
   - Download and install JDK6 from oracle.com


2. Installation of tightdb example
----------------------------------
1. Download and extract tightdb example to a directory e.g. "./tightdb".

2. Create new workspace directory "./tightdb/workspace". 
   - Open eclipse and select above directory

3. Import Example:
   Select "File->Import". 
   Choose "Maven->Existing Maven Project". Root directory: Browse for "tightdb-example"

4. Configure JDK6 on your project.
   - Ensure JDK6 is selected in Window -> Preferences -> Java -> Installed JREs
   - If it's not there, use the "Search" command on the same screen 
     and navigate to the folder where it was installed.
   - Make sure the project is configured to use the JDK6 by right-clicking "tightdb-example" 
     and select "Properties"; 
     - Select "Java Build Path" and ensure "JRE System Library [JavaSE-1.6]" is there
     - Select "Java Compiler" and select "Use complience from execution environment 'JavaSE-1.6'


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

3. Refresh the project (F5) and the generated classes are available with full autocompletion etc.

You can now run your application again.


Enjoy!
/The Tightdb team


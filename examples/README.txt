TightDB example instructions
============================

Below are instructions for testing the TightDB intro-example application in Java using Eclipse or 
further down using a command-line.

If you wish to integrate TightDB in a _new_ application using Maven or Ant (and any IDE):
  - for Maven-based projects please read "maven_setup.txt",
  - for Ant-based projects please read "ant_setup.txt".

If you wish to configure Eclipse for optimal TightDB experience in an _existing_ application, please read "eclipse_setup.txt".



==================
Short introduction
==================

TightDB for Java comes packaged into several files:
- a native, binary library (.dll, .so, etc.),
- tightdb.jar - the TightDB run-time that uses the native library,
- tightdb-devkit.jar - TightDB's APT-based tool for annotation-driven code generation at compile-time (NOT needed at run-time).

        
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
2.1. Download and install the TightDB project.

2.2. Open eclipse and select some workspace directory.

2.3. Import the example:
- Select "File -> Import". 
- Choose "General -> Existing Projects into Workspace".
- Select root directory: Browse for the "intro-example" folder and select it.


3. Running the application
--------------------------

3.1. Run the application:
   - Select "Run -> Run", "Run as Java Application" and select "tutorial" (or Showcase and Performance),
     and the output from the application will be shown in the Console window.


4. Generating TightDB classes
-----------------------------
You can now change the example as you wish. If you change or add TightDB tables, 
you need to regenerate the classes for the type-safe DSL that TightDB provides based on the @Table specifications.

4.1. All you have to do is save your files, and the generated files will be updated.

4.2. Sometimes you might need to refresh the project (F5) if the most recent changes aren't reflected.
The generated classes will be available with full auto-completion etc.

Now you can run your application again.


   ---- OO ----


===========================================================================
Buiding and running TightDB's intro-example from the command line using Ant
===========================================================================

- Navigate to the "intro-example" folder:
cd <path-to-the-examples>/intro-example

- Build & run:
ant build
ant showcase
ant performance


FAQ:
====
Q1: I get the following error when generating the TightDB classes:
"[ERROR] JVM is not suitable for processing annotation! ToolProvider.getSystemJavaCompiler() is null."
A1: Install and Configure the project to use JDK6 or JDK7.



Feedback is more than welcome!
Don't hesitate to contact us at support@tightdb.com.

Enjoy!
/The TightDB team

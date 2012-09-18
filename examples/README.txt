
TightDB example instructions
============================

Below are instructions for testing the TightDB example applications in Java using Ant or Eclipse.

Integrating Tightdb in existing or new applications
---------------------------------------------------
If you wish to integrate TightDB in a _new_ application using Maven or Ant (and any IDE):
  - for Maven-based projects please read "maven_setup.txt",
  - for Ant-based projects please read "ant_setup.txt".

If you wish to configure Eclipse for optimal TightDB experience in an _existing_ application, please read "eclipse_setup.txt".


Installing TightDB
==================
1. Install JDK6 or JDK7

2. Download TightDB

3. Build TightDB
See the README.md file in the unpacked package.

Building TightDB for Java results in 3 files: 
- a native, binary library (.dll, .so, .libjni),
- tightdb.jar - the TightDB run-time that uses the native library,
- tightdb-devkit.jar - TightDB's APT-based tool for annotation-driven code generation at compile-time (NOT needed at run-time).

Below instructions for running the examples works whether you have only built TightDB or if you have also installed it.


TightDB example applications
============================

The TightDB installation will provide a folder for Java examples:
- cd tightdb_java2/examples/intro-example

It contains 3 examples:
1. "tutorial", which is the exact code you see in the tutorial documentation.
2. "Showcase", which shows a a little more of TightDB's features.
3. "Performance", which tests TightDB, native Java Arrays and SQLite in a simple performance test.


Running TightDB example using Ant
=================================

1. Build
- ant build

2. Run
- ant tutorial
- ant showcase
- ant performance

3. Generating TightDB classes
- ant generate-sources


Running TightDB example using Eclipse
=====================================

1. Build
- Open eclipse and select an existing or new workspace directory.
- Import example
    - Select "File -> Import". 
    - Choose "General -> Existing Projects into Workspace".
    - Select root directory: Browse for the "intro-example" folder and select it.

2. Run
- Select "Run -> Run", "Run as Java Application" and select "tutorial" (or another example)

3. Generating TightDB classes
You can now change the example as you wish. If you change or add TightDB tables with the @Table annotation, classes are automatically generated for you when you save a file.
The generated classes will be available with full auto-completion etc.



Feedback is more than welcome!
Don't hesitate to contact us at support@tightdb.com.

Enjoy!
/The TightDB team

tightdb_java2
=============

This project contains both the TightDB library and some examples, too.

Description of packages:
com.tightdb - core JNI API classes and minimal test application for it (TableTest)
com.tightdb.example - hand-written examples (Example, ManualWorkingExample and WorkingExample) of usage of the API
com.tightdb.example.generated - hand-written examples of how the generated code should look like (based on ManualWorkingExample)
com.tightdb.generated - automatically generated files from the annotated classes in WorkingExample
com.tightdb.generator - code generation logic (related to JAnnocessor and customize.vm in src/main/resources)
com.tightdb.lib - the TightDB Java library what wraps around the JNI API, the generated code extends these classes
org.jannocessor.config - code generation configuration (JAnnocessor-related package and class names by convention)


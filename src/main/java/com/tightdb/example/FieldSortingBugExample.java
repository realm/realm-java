package com.tightdb.example;

import com.tightdb.lib.Table;

/**
 * The SpecReader throws error:
 * The file doesn't exist: ...\src\main\java\com\tightdb\example\buggyModel.java
 * It must be improved to search through all java files in the inferred package in such cases.
 */
@Table
class buggyModel {
	String b;
	boolean c;
	int a;
}

public class FieldSortingBugExample {

}

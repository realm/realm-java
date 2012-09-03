package com.tightdb.test;

import java.util.Date;

import com.tightdb.Table;

/**
 * This model is used to generate classes that are used only for the tests.
 */
public class TestModel {

	@Table
	class TestEmployee {
		String firstName;
		String lastName;
		int salary;
		boolean driver;
		byte[] photo;
		Date birthdate;
		Object extra;
		TestPhone phones;
	}

	@Table
	class TestPhone {
		String type;
		String number;
	}

}

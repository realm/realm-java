package com.tightdb.test;

import java.nio.ByteBuffer;
import java.util.Date;

import com.tightdb.Mixed;

public class EmployeeData {

	public String firstName;
	public String lastName;
	public int salary;
	public boolean driver;
	public byte[] photo;
	public Date birthdate;
	public Object extra;

	public EmployeeData(String firstName, String lastName, int salary, boolean driver, byte[] photo, Date birthdate, Object extra) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.salary = salary;
		this.driver = driver;
		this.photo = photo;
		this.birthdate = birthdate;
		this.extra = extra;
	}

	public Object get(int index) {
		switch (index) {
		case 0:
			return firstName;
		case 1:
			return lastName;
		case 2:
			return new Long(salary);
		case 3:
			return new Boolean(driver);
		case 4:
			return ByteBuffer.wrap(photo);
		case 5:
			return birthdate;
		case 6:
			return Mixed.mixedValue(extra);
		default:
			throw new IllegalArgumentException("Incorrect index: " + index);
		}
	}

}

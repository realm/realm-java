package com.tightdb.test;

import java.util.Date;

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

}

package com.tightdb.test;

import java.util.Date;

public class EmployeesFixture {

	public static final EmployeeData EMPLOYEE0 = new EmployeeData("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(111111), "extra");
	public static final EmployeeData EMPLOYEE1 = new EmployeeData("Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(2222), 1234);
	public static final EmployeeData EMPLOYEE2 = new EmployeeData("Johny", "B. Good", 10000, true, new byte[] { 1, 2, 3 }, new Date(333343333), true);

}

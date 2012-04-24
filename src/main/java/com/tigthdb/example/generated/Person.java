package com.tigthdb.example.generated;

import com.tigthdb.lib.AbstractCursor;
import com.tigthdb.lib.IntColumn;
import com.tigthdb.lib.StringColumn;

public class Person extends AbstractCursor<Person> {

	public final StringColumn<Person, PersonQuery> firstName = null;

	public final StringColumn<Person, PersonQuery> lastName = null;

	public final IntColumn<Person, PersonQuery> salary = null;

	public final PhoneTable phones = null;

	public String getFirstName() {
		return this.firstName.get();
	}

	public void setFirstName(String firstName) {
		this.firstName.set(firstName);
	}

	public String getLastName() {
		return this.lastName.get();
	}

	public void setLastName(String lastName) {
		this.lastName.set(lastName);
	}

	public int getSalary() {
		return this.salary.get();
	}

	public void setSalary(int salary) {
		this.salary.set(salary);
	}

}
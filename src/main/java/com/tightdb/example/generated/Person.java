package com.tightdb.example.generated;

import com.tightdb.TableBase;
import com.tightdb.lib.AbstractCursor;
import com.tightdb.lib.LongColumn;
import com.tightdb.lib.StringColumn;

public class Person extends AbstractCursor<Person> {

	public final StringColumn<Person, PersonQuery> firstName;
	public final StringColumn<Person, PersonQuery> lastName;
	public final LongColumn<Person, PersonQuery> salary;
	public final PhoneTable phones = null;

	public Person(TableBase table, long position) {
		super(table, Person.class, position);
		firstName = new StringColumn<Person, PersonQuery>(table, this, 0, "firstName");
		lastName = new StringColumn<Person, PersonQuery>(table, this, 1, "lastName");
		salary = new LongColumn<Person, PersonQuery>(table, this, 2, "salary");
	}

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

	public long getSalary() {
		return this.salary.get();
	}

	public void setSalary(long salary) {
		this.salary.set(salary);
	}

}
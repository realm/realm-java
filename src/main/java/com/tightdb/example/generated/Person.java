package com.tightdb.example.generated;

import com.tightdb.TableBase;
import com.tightdb.lib.AbstractColumn;
import com.tightdb.lib.AbstractCursor;
import com.tightdb.lib.BinaryColumn;
import com.tightdb.lib.BooleanColumn;
import com.tightdb.lib.DateColumn;
import com.tightdb.lib.LongColumn;
import com.tightdb.lib.MixedColumn;
import com.tightdb.lib.StringColumn;
import com.tightdb.lib.TableColumn;

public class Person extends AbstractCursor<Person> {

	public final StringColumn<Person, PersonQuery> firstName;
	public final StringColumn<Person, PersonQuery> lastName;
	public final LongColumn<Person, PersonQuery> salary;
	public final BooleanColumn<Person, PersonQuery> driver;
	public final BinaryColumn<Person, PersonQuery> photo;
	public final DateColumn<Person, PersonQuery> birthdate;
	public final MixedColumn<Person, PersonQuery> extra;
	public final TableColumn<Person, PersonQuery, PhoneTable> phones;

	public Person(TableBase table, long position) {
		super(table, Person.class, position);
		firstName = new StringColumn<Person, PersonQuery>(table, this, 0, "firstName");
		lastName = new StringColumn<Person, PersonQuery>(table, this, 1, "lastName");
		salary = new LongColumn<Person, PersonQuery>(table, this, 2, "salary");
		driver = new BooleanColumn<Person, PersonQuery>(table, this, 3, "driver");
		photo = new BinaryColumn<Person, PersonQuery>(table, this, 4, "photo");
		birthdate = new DateColumn<Person, PersonQuery>(table, this, 5, "birthdate");
		extra = new MixedColumn<Person, PersonQuery>(table, this, 6, "extra");
		phones = new TableColumn<Person, PersonQuery, PhoneTable>(table, 7, "phones", PhoneTable.class);
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

	@Override
	public AbstractColumn<?, ?, ?>[] columns() {
		return getColumnsArray(firstName, lastName, salary, driver, photo, birthdate, extra, phones);
	}

}
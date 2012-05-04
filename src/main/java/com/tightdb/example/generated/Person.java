package com.tightdb.example.generated;

import com.tightdb.TableBase;
import com.tightdb.lib.AbstractColumn;
import com.tightdb.lib.AbstractCursor;
import com.tightdb.lib.BinaryCursorColumn;
import com.tightdb.lib.BooleanCursorColumn;
import com.tightdb.lib.DateCursorColumn;
import com.tightdb.lib.LongCursorColumn;
import com.tightdb.lib.MixedCursorColumn;
import com.tightdb.lib.StringCursorColumn;
import com.tightdb.lib.TableCursorColumn;

public class Person extends AbstractCursor<Person> {

	public final StringCursorColumn<Person, PersonQuery> firstName;
	public final StringCursorColumn<Person, PersonQuery> lastName;
	public final LongCursorColumn<Person, PersonQuery> salary;
	public final BooleanCursorColumn<Person, PersonQuery> driver;
	public final BinaryCursorColumn<Person, PersonQuery> photo;
	public final DateCursorColumn<Person, PersonQuery> birthdate;
	public final MixedCursorColumn<Person, PersonQuery> extra;
	public final TableCursorColumn<Person, PersonQuery, PhoneTable> phones;

	public Person(TableBase table, long position) {
		super(table, Person.class, position);
		firstName = new StringCursorColumn<Person, PersonQuery>(PersonTable.TYPES, table, this, 0, "firstName");
		lastName = new StringCursorColumn<Person, PersonQuery>(PersonTable.TYPES, table, this, 1, "lastName");
		salary = new LongCursorColumn<Person, PersonQuery>(PersonTable.TYPES, table, this, 2, "salary");
		driver = new BooleanCursorColumn<Person, PersonQuery>(PersonTable.TYPES, table, this, 3, "driver");
		photo = new BinaryCursorColumn<Person, PersonQuery>(PersonTable.TYPES, table, this, 4, "photo");
		birthdate = new DateCursorColumn<Person, PersonQuery>(PersonTable.TYPES, table, this, 5, "birthdate");
		extra = new MixedCursorColumn<Person, PersonQuery>(PersonTable.TYPES, table, this, 6, "extra");
		phones = new TableCursorColumn<Person, PersonQuery, PhoneTable>(PersonTable.TYPES, table, this, 7, "phones", PhoneTable.class);
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
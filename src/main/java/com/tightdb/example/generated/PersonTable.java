package com.tightdb.example.generated;

import com.tightdb.lib.AbstractTable;
import com.tightdb.lib.LongColumn;
import com.tightdb.lib.StringColumn;

public class PersonTable extends AbstractTable<Person, PersonView> {

	public PersonTable() {
		super(Person.class, PersonView.class);
		registerStringColumn("firstName");
		registerStringColumn("lastName");
		registerIntColumn("salary");
	}

	public final StringColumn<Person, PersonQuery> firstName = new StringColumn<Person, PersonQuery>(table, 0, "firstName");

	public final StringColumn<Person, PersonQuery> lastName = new StringColumn<Person, PersonQuery>(table, 1, "lastName");

	public final LongColumn<Person, PersonQuery> salary = new LongColumn<Person, PersonQuery>(table, 2, "salary");

	public final PhoneTable phones = new PhoneTable();

	public Person add(String firstName, String lastName, int salary) {
		try {
			int position = size();

			table.insertString(0, position, firstName);
			table.insertString(1, position, lastName);
			table.insertLong(2, position, salary);
			table.insertDone();

			return cursor(position);
		} catch (Exception e) {
			throw addRowException(e);
		}
	}

	public Person insert(long position, String firstName, String lastName, int salary) {
		try {
			int pos = (int) position; // FIXME: should be improved soon

			table.insertString(0, pos, firstName);
			table.insertString(1, pos, lastName);
			table.insertLong(2, pos, salary);
			table.insertDone();

			return cursor(position);
		} catch (Exception e) {
			throw insertRowException(e);
		}
	}

}

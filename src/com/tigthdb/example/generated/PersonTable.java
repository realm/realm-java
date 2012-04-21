package com.tigthdb.example.generated;

import com.tigthdb.lib.AbstractTable;
import com.tigthdb.lib.IntColumn;
import com.tigthdb.lib.StringColumn;

public class PersonTable extends AbstractTable<Person, PersonView> {

	public final StringColumn<Person, PersonQuery> firstName = new StringColumn<Person, PersonQuery>();

	public final StringColumn<Person, PersonQuery> lastName = new StringColumn<Person, PersonQuery>();

	public final IntColumn<Person, PersonQuery> salary = new IntColumn<Person, PersonQuery>();

	public final PhoneTable phones = new PhoneTable();

	public Person add(String firstName, String lastName, int salary) {
		return null;
	}

	public Person insert(long position, String firstName, String lastName, int salary) {
		return null;
	}

	public Person at(long position) {
		return null;
	}

	public void remove(long id) {

	}

}

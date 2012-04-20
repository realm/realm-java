package com.tigthdb.example.generated;

import java.util.Iterator;

import com.tigthdb.lib.IntegerColumn;
import com.tigthdb.lib.StringColumn;

public class PersonTable implements Iterable<Person> {

	public final StringColumn<Person, PersonQuery> firstName = new StringColumn<Person, PersonQuery>();

	public final StringColumn<Person, PersonQuery> lastName = new StringColumn<Person, PersonQuery>();

	public final IntegerColumn<Person, PersonQuery> salary = new IntegerColumn<Person, PersonQuery>();
	
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

	public Iterator<Person> iterator() {
		return null;
	}

}

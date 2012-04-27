package com.tightdb.example.generated;

import com.tightdb.lib.AbstractQuery;
import com.tightdb.lib.LongColumn;
import com.tightdb.lib.StringColumn;

public class PersonQuery extends AbstractQuery {

	public final StringColumn<Person, PersonQuery> firstName = null;

	public final StringColumn<Person, PersonQuery> lastName = null;

	public final LongColumn<Person, PersonQuery> salary = null;

	public PersonView findAll() {
		return null;
	}

	public Person findFirst() {
		return null;
	}

	public Person findLast() {
		return null;
	}

	public Person findUnique() {
		return null;
	}

	// FIXME: we need other class
	public Person or() {
		return null;
	}

	public long remove() {
		return 0;
	}

}

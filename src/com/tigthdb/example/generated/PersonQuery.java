package com.tigthdb.example.generated;

import com.tigthdb.lib.AbstractQuery;
import com.tigthdb.lib.IntColumn;
import com.tigthdb.lib.StringColumn;

public class PersonQuery extends AbstractQuery {

	public final StringColumn<Person, PersonQuery> firstName = null;

	public final StringColumn<Person, PersonQuery> lastName = null;

	public final IntColumn<Person, PersonQuery> salary = null;

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
	public PersonSubQuery or() {
		return null;
	}

	public long remove() {
		return 0;
	}

}

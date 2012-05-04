package com.tightdb.example.generated;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;
import com.tightdb.lib.AbstractQuery;
import com.tightdb.lib.LongRowsetColumn;
import com.tightdb.lib.StringRowsetColumn;

public class PersonQuery extends AbstractQuery<Person, PersonView> {

	public final StringRowsetColumn<Person, PersonQuery> firstName;

	public final StringRowsetColumn<Person, PersonQuery> lastName;

	public final LongRowsetColumn<Person, PersonQuery> salary;

	public PersonQuery(TableBase table, TableQuery query) {
		super(table, query);
		firstName = new StringRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 0, "firstName");
		lastName = new StringRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 1, "lastName");
		salary = new LongRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 2, "salary");
	}

}

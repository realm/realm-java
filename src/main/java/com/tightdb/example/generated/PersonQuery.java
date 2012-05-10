package com.tightdb.example.generated;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;
import com.tightdb.generated.PhoneTable;
import com.tightdb.lib.AbstractQuery;
import com.tightdb.lib.BinaryQueryColumn;
import com.tightdb.lib.BooleanQueryColumn;
import com.tightdb.lib.DateQueryColumn;
import com.tightdb.lib.LongQueryColumn;
import com.tightdb.lib.MixedQueryColumn;
import com.tightdb.lib.StringQueryColumn;
import com.tightdb.lib.TableQueryColumn;

public class PersonQuery extends AbstractQuery<PersonQuery, Person, PersonView> {

	public final StringQueryColumn<Person, PersonQuery> firstName;
	public final StringQueryColumn<Person, PersonQuery> lastName;
	public final LongQueryColumn<Person, PersonQuery> salary;
	public final BooleanQueryColumn<Person, PersonQuery> driver;
	public final BinaryQueryColumn<Person, PersonQuery> photo;
	public final DateQueryColumn<Person, PersonQuery> birthdate;
	public final MixedQueryColumn<Person, PersonQuery> extra;
	public final TableQueryColumn<Person, PersonQuery, PhoneTable> phones;

	public PersonQuery(TableBase table, TableQuery query) {
		super(PersonTable.TYPES, table, query);
		firstName = new StringQueryColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 0, "firstName");
		lastName = new StringQueryColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 1, "lastName");
		salary = new LongQueryColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 2, "salary");
		driver = new BooleanQueryColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 3, "driver");
		photo = new BinaryQueryColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 4, "photo");
		birthdate = new DateQueryColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 5, "birthdate");
		extra = new MixedQueryColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 6, "extra");
		phones = new TableQueryColumn<Person, PersonQuery, PhoneTable>(PersonTable.TYPES, table, query, 7, "phones", PhoneTable.class);
	}

}

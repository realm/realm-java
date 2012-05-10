package com.tightdb.example.generated;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;
import com.tightdb.generated.PhoneTable;
import com.tightdb.lib.AbstractQuery;
import com.tightdb.lib.BinaryRowsetColumn;
import com.tightdb.lib.BooleanRowsetColumn;
import com.tightdb.lib.DateRowsetColumn;
import com.tightdb.lib.LongRowsetColumn;
import com.tightdb.lib.MixedRowsetColumn;
import com.tightdb.lib.StringRowsetColumn;
import com.tightdb.lib.TableRowsetColumn;

public class PersonQuery extends AbstractQuery<Person, PersonView> {

	public final StringRowsetColumn<Person, PersonQuery> firstName;
	public final StringRowsetColumn<Person, PersonQuery> lastName;
	public final LongRowsetColumn<Person, PersonQuery> salary;
	public final BooleanRowsetColumn<Person, PersonQuery> driver;
	public final BinaryRowsetColumn<Person, PersonQuery> photo;
	public final DateRowsetColumn<Person, PersonQuery> birthdate;
	public final MixedRowsetColumn<Person, PersonQuery> extra;
	public final TableRowsetColumn<Person, PersonQuery, PhoneTable> phones;

	public PersonQuery(TableBase table, TableQuery query) {
		super(PersonTable.TYPES, table, query);
		firstName = new StringRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 0, "firstName");
		lastName = new StringRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 1, "lastName");
		salary = new LongRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 2, "salary");
		driver = new BooleanRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 3, "driver");
		photo = new BinaryRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 4, "photo");
		birthdate = new DateRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 5, "birthdate");
		extra = new MixedRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, table, query, 6, "extra");
		phones = new TableRowsetColumn<Person, PersonQuery, PhoneTable>(PersonTable.TYPES, table, query, 7, "phones", PhoneTable.class);
	}

}

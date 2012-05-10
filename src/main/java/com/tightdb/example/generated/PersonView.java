package com.tightdb.example.generated;

import com.tightdb.TableViewBase;
import com.tightdb.lib.AbstractView;
import com.tightdb.lib.BinaryRowsetColumn;
import com.tightdb.lib.BooleanRowsetColumn;
import com.tightdb.lib.DateRowsetColumn;
import com.tightdb.lib.LongRowsetColumn;
import com.tightdb.lib.MixedRowsetColumn;
import com.tightdb.lib.StringRowsetColumn;
import com.tightdb.lib.TableRowsetColumn;

public class PersonView extends AbstractView<Person, PersonView> {

	public final StringRowsetColumn<Person, PersonQuery> firstName = new StringRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, rowset, 0, "firstName");
	public final StringRowsetColumn<Person, PersonQuery> lastName = new StringRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, rowset, 1, "lastName");
	public final LongRowsetColumn<Person, PersonQuery> salary = new LongRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, rowset, 2, "salary");
	public final BooleanRowsetColumn<Person, PersonQuery> driver = new BooleanRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, rowset, 3, "driver");
	public final BinaryRowsetColumn<Person, PersonQuery> photo = new BinaryRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, rowset, 4, "photo");
	public final DateRowsetColumn<Person, PersonQuery> birthdate = new DateRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, rowset, 5, "birthdate");
	public final MixedRowsetColumn<Person, PersonQuery> extra = new MixedRowsetColumn<Person, PersonQuery>(PersonTable.TYPES, rowset, 6, "extra");
	public final TableRowsetColumn<Person, PersonQuery, PhoneTable> phones = new TableRowsetColumn<Person, PersonQuery, PhoneTable>(PersonTable.TYPES, rowset, 7, "phones", PhoneTable.class);

	public PersonView(TableViewBase viewBase) {
		super(PersonTable.TYPES, viewBase);
	}
	
}

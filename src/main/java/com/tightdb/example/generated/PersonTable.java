package com.tightdb.example.generated;

import java.io.Serializable;
import java.util.Date;

import com.tightdb.TableSpec;
import com.tightdb.lib.AbstractTable;
import com.tightdb.lib.BinaryRowsetColumn;
import com.tightdb.lib.BooleanRowsetColumn;
import com.tightdb.lib.DateRowsetColumn;
import com.tightdb.lib.EntityTypes;
import com.tightdb.lib.LongRowsetColumn;
import com.tightdb.lib.MixedRowsetColumn;
import com.tightdb.lib.StringRowsetColumn;
import com.tightdb.lib.TableRowsetColumn;

public class PersonTable extends AbstractTable<Person, PersonView, PersonQuery> {

	public static final EntityTypes<PersonTable, PersonView, Person, PersonQuery> TYPES = new EntityTypes<PersonTable, PersonView, Person, PersonQuery>(PersonTable.class, PersonView.class, Person.class, PersonQuery.class);

	public final StringRowsetColumn<Person, PersonQuery> firstName = new StringRowsetColumn<Person, PersonQuery>(TYPES, table, 0, "firstName");
	public final StringRowsetColumn<Person, PersonQuery> lastName = new StringRowsetColumn<Person, PersonQuery>(TYPES, table, 1, "lastName");
	public final LongRowsetColumn<Person, PersonQuery> salary = new LongRowsetColumn<Person, PersonQuery>(TYPES, table, 2, "salary");
	public final BooleanRowsetColumn<Person, PersonQuery> driver = new BooleanRowsetColumn<Person, PersonQuery>(TYPES, table, 3, "driver");
	public final BinaryRowsetColumn<Person, PersonQuery> photo = new BinaryRowsetColumn<Person, PersonQuery>(TYPES, table, 4, "photo");
	public final DateRowsetColumn<Person, PersonQuery> birthdate = new DateRowsetColumn<Person, PersonQuery>(TYPES, table, 5, "birthdate");
	public final MixedRowsetColumn<Person, PersonQuery> extra = new MixedRowsetColumn<Person, PersonQuery>(TYPES, table, 6, "extra");
	public final TableRowsetColumn<Person, PersonQuery, PhoneTable> phones = new TableRowsetColumn<Person, PersonQuery, PhoneTable>(TYPES, table, 7, "phones", PhoneTable.class);

	public PersonTable() {
		super(TYPES);
	}

	@Override
	protected void specifyStructure(TableSpec spec) {
		registerStringColumn(spec, "firstName");
		registerStringColumn(spec, "lastName");
		registerLongColumn(spec, "salary");
		registerBooleanColumn(spec, "driver");
		registerBinaryColumn(spec, "photo");
		registerDateColumn(spec, "birthdate");
		registerMixedColumn(spec, "extra");
		registerTableColumn(spec, "attributes", new PhoneTable(null));
	}

	public Person add(String firstName, String lastName, int salary, boolean driver, byte[] photo, Date birthdate, Serializable extra) {
		try {
			long position = size();

			insertString(0, position, firstName);
			insertString(1, position, lastName);
			insertLong(2, position, salary);
			insertBoolean(3, position, driver);
			insertBinary(4, position, photo);
			insertDate(5, position, birthdate);
			insertMixed(6, position, extra);
			insertTable(7, position);

			insertDone();

			return cursor(position);
		} catch (Exception e) {
			throw addRowException(e);
		}
	}

	public Person insert(long position, String firstName, String lastName, int salary, boolean driver, byte[] photo, Date birthdate,
			Serializable extra) {
		try {
			insertString(0, position, firstName);
			insertString(1, position, lastName);
			insertLong(2, position, salary);
			insertBoolean(3, position, driver);
			insertBinary(4, position, photo);
			insertDate(5, position, birthdate);
			insertMixed(6, position, extra);
			insertTable(7, position);
			insertDone();

			return cursor(position);
		} catch (Exception e) {
			throw insertRowException(e);
		}
	}

}

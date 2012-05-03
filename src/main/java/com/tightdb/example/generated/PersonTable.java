package com.tightdb.example.generated;

import java.io.Serializable;
import java.util.Date;

import com.tightdb.TableSpec;
import com.tightdb.lib.AbstractTable;
import com.tightdb.lib.BinaryRowsetColumn;
import com.tightdb.lib.BooleanRowsetColumn;
import com.tightdb.lib.DateRowsetColumn;
import com.tightdb.lib.LongRowsetColumn;
import com.tightdb.lib.MixedRowsetColumn;
import com.tightdb.lib.StringRowsetColumn;
import com.tightdb.lib.TableRowsetColumn;

public class PersonTable extends AbstractTable<Person, PersonView> {

	public PersonTable() {
		super(Person.class, PersonView.class);
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
		registerTableColumn(spec, "attributes", new PhoneTable());
	}

	public final StringRowsetColumn<Person, PersonQuery> firstName = new StringRowsetColumn<Person, PersonQuery>(table, 0, "firstName");

	public final StringRowsetColumn<Person, PersonQuery> lastName = new StringRowsetColumn<Person, PersonQuery>(table, 1, "lastName");

	public final LongRowsetColumn<Person, PersonQuery> salary = new LongRowsetColumn<Person, PersonQuery>(table, 2, "salary");

	public final BooleanRowsetColumn<Person, PersonQuery> driver = new BooleanRowsetColumn<Person, PersonQuery>(table, 3, "driver");

	public final BinaryRowsetColumn<Person, PersonQuery> photo = new BinaryRowsetColumn<Person, PersonQuery>(table, 4, "photo");

	public final DateRowsetColumn<Person, PersonQuery> birthdate = new DateRowsetColumn<Person, PersonQuery>(table, 5, "birthdate");

	public final MixedRowsetColumn<Person, PersonQuery> extra = new MixedRowsetColumn<Person, PersonQuery>(table, 6, "extra");

	public final TableRowsetColumn<Person, PersonQuery, PhoneTable> phones = new TableRowsetColumn<Person, PersonQuery, PhoneTable>(table, 7, "phones", PhoneTable.class);

	public Person add(String firstName, String lastName, int salary, boolean driver, byte[] photo, Date birthdate, Serializable extra) {
		try {
			int position = size();

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

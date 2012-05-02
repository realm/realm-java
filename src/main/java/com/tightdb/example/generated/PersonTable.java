package com.tightdb.example.generated;

import java.io.Serializable;
import java.util.Date;

import com.tightdb.TableSpec;
import com.tightdb.lib.AbstractTable;
import com.tightdb.lib.BinaryColumn;
import com.tightdb.lib.BooleanColumn;
import com.tightdb.lib.DateColumn;
import com.tightdb.lib.LongColumn;
import com.tightdb.lib.MixedColumn;
import com.tightdb.lib.StringColumn;
import com.tightdb.lib.TableColumn;

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

	public final StringColumn<Person, PersonQuery> firstName = new StringColumn<Person, PersonQuery>(table, 0, "firstName");

	public final StringColumn<Person, PersonQuery> lastName = new StringColumn<Person, PersonQuery>(table, 1, "lastName");

	public final LongColumn<Person, PersonQuery> salary = new LongColumn<Person, PersonQuery>(table, 2, "salary");

	public final BooleanColumn<Person, PersonQuery> driver = new BooleanColumn<Person, PersonQuery>(table, 3, "driver");

	public final BinaryColumn<Person, PersonQuery> photo = new BinaryColumn<Person, PersonQuery>(table, 4, "photo");

	public final DateColumn<Person, PersonQuery> birthdate = new DateColumn<Person, PersonQuery>(table, 5, "birthdate");

	public final MixedColumn<Person, PersonQuery> extra = new MixedColumn<Person, PersonQuery>(table, 6, "extra");

	public final TableColumn<Person, PersonQuery, PhoneTable> phones = new TableColumn<Person, PersonQuery, PhoneTable>(table, 7, "phones", PhoneTable.class);

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

package com.tightdb.example.generated;

import com.tightdb.TableSpec;
import com.tightdb.lib.AbstractSubtable;
import com.tightdb.lib.StringRowsetColumn;

public class PhoneTable extends AbstractSubtable<Phone, PhoneView> {

	public PhoneTable() {
		super(Phone.class, PhoneView.class);
	}

	@Override
	protected void specifyStructure(TableSpec spec) {
		registerStringColumn(spec, "type");
		registerStringColumn(spec, "number");
	}

	public final StringRowsetColumn<Phone, PhoneQuery> type = new StringRowsetColumn<Phone, PhoneQuery>(table, 0, "type");

	public final StringRowsetColumn<Phone, PhoneQuery> number = new StringRowsetColumn<Phone, PhoneQuery>(table, 1, "number");

	public Phone add(String type, String number) {
		try {
			int position = size();

			insertString(0, position, type);
			insertString(1, position, number);

			insertDone();

			return cursor(position);
		} catch (Exception e) {
			throw addRowException(e);
		}
	}

	public Phone insert(long position, String type, String number) {
		try {
			insertString(0, position, type);
			insertString(1, position, number);

			insertDone();

			return cursor(position);
		} catch (Exception e) {
			throw insertRowException(e);
		}
	}

}

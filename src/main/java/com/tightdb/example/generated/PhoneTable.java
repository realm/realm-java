package com.tightdb.example.generated;

import com.tightdb.lib.AbstractTable;
import com.tightdb.lib.StringColumn;

public class PhoneTable extends AbstractTable<Phone, PhoneView> {

	public PhoneTable() {
		super(Phone.class, PhoneView.class);
		registerStringColumn("type");
		registerStringColumn("number");
	}

	public final StringColumn<Phone, PhoneQuery> type = new StringColumn<Phone, PhoneQuery>(table, 0, "type");

	public final StringColumn<Phone, PhoneQuery> number = new StringColumn<Phone, PhoneQuery>(table, 1, "number");

	public Phone add(String type, String number) {
		return null;
	}

	public Phone insert(long position, String type, String number) {
		return null;
	}

}

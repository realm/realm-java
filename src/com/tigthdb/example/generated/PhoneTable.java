package com.tigthdb.example.generated;

import com.tigthdb.lib.AbstractTable;
import com.tigthdb.lib.StringColumn;

public class PhoneTable extends AbstractTable<Phone> {

	public final StringColumn<Phone, PhoneQuery> type = new StringColumn<Phone, PhoneQuery>();

	public final StringColumn<Phone, PhoneQuery> number = new StringColumn<Phone, PhoneQuery>();

	public Phone add(String type, String number) {
		return null;
	}

	public Phone insert(long position, String type, String number) {
		return null;
	}

	public Phone at(long position) {
		return null;
	}

	public void remove(long id) {

	}

}

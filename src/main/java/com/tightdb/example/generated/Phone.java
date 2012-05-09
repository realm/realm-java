package com.tightdb.example.generated;

import com.tightdb.lib.AbstractCursor;
import com.tightdb.lib.IRowsetBase;
import com.tightdb.lib.StringCursorColumn;

public class Phone extends AbstractCursor<Phone> {

	public Phone(IRowsetBase rowset, long position) {
		super(PhoneTable.TYPES, rowset, position);
	}

	public final StringCursorColumn<Phone, PhoneQuery> type = null;

	public final StringCursorColumn<Phone, PhoneQuery> phone = null;

	public String getType() {
		return this.type.get();
	}

	public void setType(String type) {
		this.type.set(type);
	}

	public String getPhone() {
		return this.phone.get();
	}

	public void setPhone(String phone) {
		this.phone.set(phone);
	}

}

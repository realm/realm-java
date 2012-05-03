package com.tightdb.example.generated;

import com.tightdb.TableBase;
import com.tightdb.lib.AbstractCursor;
import com.tightdb.lib.StringRowsetColumn;

public class Phone extends AbstractCursor<Phone> {

	public Phone(TableBase table, long position) {
		super(table, Phone.class, position);
	}

	public final StringRowsetColumn<Phone, PhoneQuery> type = null;

	public final StringRowsetColumn<Phone, PhoneQuery> phone = null;

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

package com.tigthdb.example.generated;

import com.tightdb.TableBase;
import com.tigthdb.lib.AbstractCursor;
import com.tigthdb.lib.StringColumn;

public class Phone extends AbstractCursor<Phone> {

	public Phone(TableBase table, long position) {
		super(table, Phone.class, position);
	}

	public final StringColumn<Phone, PhoneQuery> type = null;

	public final StringColumn<Phone, PhoneQuery> phone = null;

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

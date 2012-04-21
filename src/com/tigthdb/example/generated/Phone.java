package com.tigthdb.example.generated;

import com.tigthdb.lib.AbstractCursor;
import com.tigthdb.lib.StringColumn;

public class Phone extends AbstractCursor {

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

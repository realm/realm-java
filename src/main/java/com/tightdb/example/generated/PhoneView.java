package com.tightdb.example.generated;

import com.tightdb.TableViewBase;
import com.tightdb.lib.AbstractView;
import com.tightdb.lib.StringRowsetColumn;

public class PhoneView extends AbstractView<Phone, PhoneView> {

	public final StringRowsetColumn<Phone, PhoneQuery> type = null;

	public final StringRowsetColumn<Phone, PhoneQuery> phone = null;

	public PhoneView(TableViewBase viewBase) {
		super(viewBase);
	}

}

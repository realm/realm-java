package com.tightdb.example.generated;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;
import com.tightdb.lib.AbstractQuery;
import com.tightdb.lib.StringRowsetColumn;

public class PhoneQuery extends AbstractQuery<Phone, PhoneView> {

	public final StringRowsetColumn<Phone, PhoneQuery> type = null;

	public final StringRowsetColumn<Phone, PhoneQuery> phone = null;

	public PhoneQuery(TableBase table, TableQuery query2) {
		super(table, query2);
	}

}

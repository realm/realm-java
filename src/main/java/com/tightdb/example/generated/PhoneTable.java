package com.tightdb.example.generated;

import com.tightdb.SubTableBase;
import com.tightdb.TableSpec;
import com.tightdb.lib.AbstractSubtable;
import com.tightdb.lib.EntityTypes;
import com.tightdb.lib.StringRowsetColumn;

public class PhoneTable extends AbstractSubtable<Phone, PhoneView, PhoneQuery> {

	public static final EntityTypes<PhoneTable, PhoneView, Phone, PhoneQuery> TYPES = new EntityTypes<PhoneTable, PhoneView, Phone, PhoneQuery>(PhoneTable.class, PhoneView.class, Phone.class, PhoneQuery.class); 

	public final StringRowsetColumn<Phone, PhoneQuery> type = new StringRowsetColumn<Phone, PhoneQuery>(TYPES, table, null, 0, "type");

	public final StringRowsetColumn<Phone, PhoneQuery> number = new StringRowsetColumn<Phone, PhoneQuery>(TYPES, table, null, 1, "number");
	
	public PhoneTable(SubTableBase subTableBase) {
		super(Phone.class, PhoneView.class, PhoneQuery.class, subTableBase);
	}

	@Override
	protected void specifyStructure(TableSpec spec) {
		registerStringColumn(spec, "type");
		registerStringColumn(spec, "number");
	}
	
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

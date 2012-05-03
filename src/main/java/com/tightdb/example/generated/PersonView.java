package com.tightdb.example.generated;

import com.tightdb.lib.AbstractView;
import com.tightdb.lib.LongRowsetColumn;
import com.tightdb.lib.StringRowsetColumn;

public class PersonView extends AbstractView<Person, PersonView> {

	public final StringRowsetColumn<Person, PersonQuery> firstName = null;

	public final StringRowsetColumn<Person, PersonQuery> lastName = null;

	public final LongRowsetColumn<Person, PersonQuery> salary = null;

	public final PhoneTable phones = null;

}

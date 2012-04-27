package com.tightdb.example.generated;

import com.tightdb.lib.AbstractView;
import com.tightdb.lib.LongColumn;
import com.tightdb.lib.StringColumn;

public class PersonView extends AbstractView<Person, PersonView> {

	public final StringColumn<Person, PersonQuery> firstName = null;

	public final StringColumn<Person, PersonQuery> lastName = null;

	public final LongColumn<Person, PersonQuery> salary = null;

	public final PhoneTable phones = null;

}

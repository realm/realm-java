package com.tigthdb.example.generated;

import com.tigthdb.lib.AbstractView;
import com.tigthdb.lib.LongColumn;
import com.tigthdb.lib.StringColumn;

public class PersonView extends AbstractView<Person, PersonView> {

	public final StringColumn<Person, PersonQuery> firstName = null;

	public final StringColumn<Person, PersonQuery> lastName = null;

	public final LongColumn<Person, PersonQuery> salary = null;

	public final PhoneTable phones = null;

}

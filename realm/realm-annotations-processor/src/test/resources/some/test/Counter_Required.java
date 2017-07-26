package some.test;

import io.realm.MutableRealmInteger;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Counter_Required extends RealmObject {

    @Required
    private final MutableRealmInteger columnMutableRealmInteger = MutableRealmInteger.valueOf(0);

    public MutableRealmInteger getColumnMutableRealmInteger() {
        return columnMutableRealmInteger;
    }
}

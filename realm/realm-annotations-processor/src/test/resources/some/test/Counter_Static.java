package some.test;

import io.realm.MutableRealmInteger;
import io.realm.RealmObject;

public class Counter_Static extends RealmObject {

    private static final MutableRealmInteger columnMutableRealmInteger = MutableRealmInteger.valueOf(0);

    public MutableRealmInteger getColumnMutableRealmInteger() {
        return columnMutableRealmInteger;
    }
}

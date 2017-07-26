package some.test;

import io.realm.MutableRealmInteger;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Counter_Ignored extends RealmObject {
    private int id;

    @Ignore
    private final MutableRealmInteger columnMutableRealmInteger = MutableRealmInteger.valueOf(0);

    public MutableRealmInteger getColumnMutableRealmInteger() {
        return columnMutableRealmInteger;
    }
}

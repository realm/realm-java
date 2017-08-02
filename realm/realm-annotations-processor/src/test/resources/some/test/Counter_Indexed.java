package some.test;

import io.realm.MutableRealmInteger;
import io.realm.RealmObject;
import io.realm.annotations.Index;

public class Counter_Indexed extends RealmObject {

    @Index
    private final MutableRealmInteger columnMutableRealmInteger = MutableRealmInteger.valueOf(0);

    public MutableRealmInteger getColumnMutableRealmInteger() {
        return columnMutableRealmInteger;
    }
}
